package com.safepathjdbc.servlet;

import com.safepathjdbc.dao.CrimeReportDao;
import com.safepathjdbc.model.CrimeReport;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(urlPatterns = "/crimeReports")
public class CrimeReportServlet extends HttpServlet {
    private final CrimeReportDao dao = new CrimeReportDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if ("filter".equals(action)) {
                handleFilter(req, resp);
                return;
            }

            String reportNumber = req.getParameter("reportNumber");
            String searchReportNumber = req.getParameter("searchReportNumber");
            String searchPrecinct = req.getParameter("searchPrecinct");
            String searchNeighborhood = req.getParameter("searchNeighborhood");
            
            if (reportNumber != null && !reportNumber.isEmpty()) {
                // Search by exact report number
                CrimeReport r = dao.findById(reportNumber);
                req.setAttribute("reports", r == null ? java.util.List.of() : java.util.List.of(r));
                if (r == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if ((searchReportNumber != null && !searchReportNumber.trim().isEmpty()) ||
                       (searchPrecinct != null && !searchPrecinct.trim().isEmpty()) ||
                       (searchNeighborhood != null && !searchNeighborhood.trim().isEmpty())) {
                // Search with filters
                List<CrimeReport> list = dao.search(
                    searchReportNumber != null ? searchReportNumber.trim() : null,
                    searchPrecinct != null ? searchPrecinct.trim() : null,
                    searchNeighborhood != null ? searchNeighborhood.trim() : null
                );
                req.setAttribute("reports", list);
                req.setAttribute("searchReportNumber", searchReportNumber != null ? searchReportNumber.trim() : "");
                req.setAttribute("searchPrecinct", searchPrecinct != null ? searchPrecinct.trim() : "");
                req.setAttribute("searchNeighborhood", searchNeighborhood != null ? searchNeighborhood.trim() : "");
            } else {
                // List all
                List<CrimeReport> list = dao.findAll();
                req.setAttribute("reports", list);
            }
            req.getRequestDispatcher("/WEB-INF/jsp/crimeReports.jsp").forward(req, resp);
        } catch (SQLException e) { throw new ServletException(e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                CrimeReport r = new CrimeReport();
                r.setReportNumber(req.getParameter("reportNumber"));
                String dt = req.getParameter("reportDatetime");
                r.setReportDatetime(parseDateTime(dt));
                r.setPrecinct(req.getParameter("precinct"));
                r.setSector(req.getParameter("sector"));
                r.setBeat(req.getParameter("beat"));
                r.setMcppNeighborhood(req.getParameter("mcppNeighborhood"));
                r.setBlurredAddress(req.getParameter("blurredAddress"));
                String lat = req.getParameter("blurredLatitude");
                r.setBlurredLatitude(lat == null || lat.isEmpty() ? null : Double.valueOf(lat));
                String lon = req.getParameter("blurredLongitude");
                r.setBlurredLongitude(lon == null || lon.isEmpty() ? null : Double.valueOf(lon));
                dao.create(r);
            } else if ("update".equals(action)) {
                String reportNumber = req.getParameter("reportNumber");
                CrimeReport r = dao.findById(reportNumber);
                if (r == null) throw new ServletException("Crime report not found: " + reportNumber);
                String dt = req.getParameter("reportDatetime");
                r.setReportDatetime(parseDateTime(dt));
                r.setPrecinct(req.getParameter("precinct"));
                r.setSector(req.getParameter("sector"));
                r.setBeat(req.getParameter("beat"));
                r.setMcppNeighborhood(req.getParameter("mcppNeighborhood"));
                r.setBlurredAddress(req.getParameter("blurredAddress"));
                String lat = req.getParameter("blurredLatitude");
                r.setBlurredLatitude(lat == null || lat.isEmpty() ? null : Double.valueOf(lat));
                String lon = req.getParameter("blurredLongitude");
                r.setBlurredLongitude(lon == null || lon.isEmpty() ? null : Double.valueOf(lon));
                dao.update(r);
            } else if ("delete".equals(action)) {
                dao.delete(req.getParameter("reportNumber"));
            }
            resp.sendRedirect(req.getContextPath()+"/crimeReports");
        } catch (SQLException e) { throw new ServletException(e); }
    }

    private void handleFilter(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            double lat = Double.parseDouble(req.getParameter("lat"));
            double lon = Double.parseDouble(req.getParameter("lon"));
            int radius = Integer.parseInt(req.getParameter("radius"));
            
            String crimeTypesParam = req.getParameter("crimeTypes");
            List<String> crimeTypes = null;
            if (crimeTypesParam != null && !crimeTypesParam.isEmpty()) {
                crimeTypes = java.util.Arrays.asList(crimeTypesParam.split(","));
            }

            LocalDateTime startTime = parseDateTime(req.getParameter("timeStart"));
            LocalDateTime endTime = parseDateTime(req.getParameter("timeEnd"));
            
            String limitParam = req.getParameter("limit");
            int limit = (limitParam != null && !limitParam.isEmpty()) ? Integer.parseInt(limitParam) : 200;

            List<CrimeReport> crimes = dao.getCrimesByFilter(lat, lon, radius, crimeTypes, startTime, endTime, limit);

            // Manually constructing JSON to avoid external dependencies like Gson/Jackson if not present
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"count\":").append(crimes.size()).append(",");
            json.append("\"crimes\":[");
            for (int i = 0; i < crimes.size(); i++) {
                CrimeReport r = crimes.get(i);
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"reportNumber\":\"").append(escapeJson(r.getReportNumber())).append("\",");
                json.append("\"offenseType\":\"").append(escapeJson(r.getOffenseType())).append("\",");
                json.append("\"reportDatetime\":\"").append(r.getReportDatetime() != null ? r.getReportDatetime().toString() : "").append("\",");
                json.append("\"latitude\":").append(r.getBlurredLatitude()).append(",");
                json.append("\"longitude\":").append(r.getBlurredLongitude()).append(",");
                json.append("\"neighborhood\":\"").append(escapeJson(r.getMcppNeighborhood())).append("\",");
                json.append("\"precinct\":\"").append(escapeJson(r.getPrecinct())).append("\",");
                json.append("\"sector\":\"").append(escapeJson(r.getSector())).append("\"");
                json.append("}");
            }
            json.append("]");
            json.append("}");

            resp.getWriter().write(json.toString());
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid numeric parameter\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private LocalDateTime parseDateTime(String dt) {
        if (dt == null || dt.isEmpty()) {
            return null;
        }
        try {
            // Try parsing datetime-local format: "yyyy-MM-ddTHH:mm"
            return LocalDateTime.parse(dt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            try {
                // Fallback: try ISO format
                return LocalDateTime.parse(dt);
            } catch (DateTimeParseException e2) {
                // If all else fails, return current time
                return LocalDateTime.now();
            }
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
