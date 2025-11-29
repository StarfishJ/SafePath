package com.safepathjdbc.servlet;

import com.safepathjdbc.etl.EtlProcessor;
import com.safepathjdbc.etl.EtlResult;
import com.safepathjdbc.etl.RealTimeEtlProcessor;
import com.safepathjdbc.util.ConnectionManager;

import com.safepathjdbc.etl.SpdCrimeEtlProcessor;
import com.safepathjdbc.etl.SpdCrimeEtlResult;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@WebServlet(urlPatterns = "/etl/*")
@MultipartConfig
public class EtlServlet extends HttpServlet {

    private final RealTimeEtlProcessor realTimeProcessor = new RealTimeEtlProcessor();
    private final SpdCrimeEtlProcessor spdCrimeProcessor = new SpdCrimeEtlProcessor();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if ("/realtime-911".equals(pathInfo)) {
            handleRealTimeEtl(req, resp);
        } else if ("/spd-crime".equals(pathInfo)) {
            handleSpdCrimeEtl(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"Endpoint not found\"}");
        }
    }

    private void handleSpdCrimeEtl(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Part filePart = req.getPart("file");
        if (filePart == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Missing file part\"}");
            return;
        }

        try (InputStream is = filePart.getInputStream()) {
            int sourceId = getOrCreateSource("SPD Crime Data");
            int etlId = createEtlRun(sourceId);

            // Note: We might want to pass etlId to processor in future, but interface is fixed for now.
            EtlResult result = spdCrimeProcessor.process(is, sourceId);
            
            // If result is SpdCrimeEtlResult, we can get more details
            updateEtlRun(etlId, result);

            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"status\":\"SUCCESS\",");
            json.append("\"totalRows\":").append(result.getTotalRows()).append(",");
            json.append("\"invalidRows\":").append(result.getInvalidRows()).append(",");
            json.append("\"etlId\":").append(etlId);
            
            if (result instanceof SpdCrimeEtlResult) {
                SpdCrimeEtlResult spdResult = (SpdCrimeEtlResult) result;
                json.append(",\"insertedReports\":").append(spdResult.getInsertedReports());
                json.append(",\"insertedOffenses\":").append(spdResult.getInsertedOffenses());
                json.append(",\"newOffenseTypes\":").append(spdResult.getNewOffenseTypes());
            } else {
                json.append(",\"insertedRows\":").append(result.getInsertedRows());
            }
            
            json.append("}");
            resp.getWriter().write(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleRealTimeEtl(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Part filePart = req.getPart("file");
        if (filePart == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Missing file part\"}");
            return;
        }

        try (InputStream is = filePart.getInputStream()) {
            // 1. Create Source record (or find existing)
            int sourceId = getOrCreateSource("Seattle Real Time Fire 911");

            // 2. Create ETL Run record (start)
            int etlId = createEtlRun(sourceId);

            // 3. Process
            EtlResult result = realTimeProcessor.process(is, sourceId);

            // 4. Update ETL Run record (end)
            updateEtlRun(etlId, result);

            // 5. Response
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"status\":\"SUCCESS\",");
            json.append("\"totalRows\":").append(result.getTotalRows()).append(",");
            json.append("\"insertedIncidents\":").append(result.getInsertedRows()).append(",");
            json.append("\"invalidRows\":").append(result.getInvalidRows()).append(",");
            json.append("\"etlId\":").append(etlId);
            json.append("}");
            
            resp.getWriter().write(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private int getOrCreateSource(String sourceName) throws Exception {
        String selectSql = "SELECT source_id FROM sources WHERE source_name = ?";
        String insertSql = "INSERT INTO sources (source_name, created_at) VALUES (?, ?)";
        
        try (Connection conn = ConnectionManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setString(1, sourceName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, sourceName);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        }
        throw new Exception("Failed to get or create source");
    }

    private int createEtlRun(int sourceId) throws Exception {
        String sql = "INSERT INTO etl_runs (source_id, run_time) VALUES (?, ?)";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sourceId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new Exception("Failed to create ETL run");
    }

    private void updateEtlRun(int etlId, EtlResult result) throws Exception {
        String sql = "UPDATE etl_runs SET record_count = ?, invalid_count = ? WHERE etl_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, result.getTotalRows());
            ps.setInt(2, result.getInvalidRows()); // Assuming we added invalid_count column as per design doc
            ps.setInt(3, etlId);
            ps.executeUpdate();
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
