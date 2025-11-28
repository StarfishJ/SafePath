package com.safepathjdbc.servlet;

import com.safepathjdbc.dao.CrimeReportDao;
import com.safepathjdbc.model.CrimeReport;
import com.safepathjdbc.util.DatabaseInitializer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet(urlPatterns = {"/crimeReports", "/crime-report"})
public class CrimeReportServlet extends HttpServlet {
    private final CrimeReportDao dao = new CrimeReportDao();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Double parseDouble(String v) {
        try {
            return (v == null || v.isEmpty()) ? null : Double.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Database should already be initialized by AppUserServlet (loadOnStartup=1)
            // Only initialize if not already done (fast check)
            DatabaseInitializer.initializeIfNeeded();
            String action = req.getParameter("action");

            
            // Handle JSON API requests
            if ("range".equals(action)) {

                // ⭐ 和前端一致：min_lat，而不是 minLat
                Double minLat = parseDouble(req.getParameter("min_lat"));
                Double maxLat = parseDouble(req.getParameter("max_lat"));
                Double minLon = parseDouble(req.getParameter("min_lng"));
                Double maxLon = parseDouble(req.getParameter("max_lng"));

                // ⭐ 默认 100 天：前端传 start_date / end_date
                LocalDateTime startTime = parseDateTime(req.getParameter("start_date"));
                LocalDateTime endTime = parseDateTime(req.getParameter("end_date"));

                List<CrimeReport> all = dao.findAll();
                List<Map<String, Object>> results = new ArrayList<>();

                for (CrimeReport r : all) {
                    boolean ok = true;

                    // 时间过滤
                    if (startTime != null && r.getReportDatetime() != null &&
                            r.getReportDatetime().isBefore(startTime)) ok = false;
                    if (endTime != null && r.getReportDatetime() != null &&
                            r.getReportDatetime().isAfter(endTime)) ok = false;

                    // 经纬度过滤
                    if (minLat != null && r.getBlurredLatitude() != null &&
                            r.getBlurredLatitude() < minLat) ok = false;
                    if (maxLat != null && r.getBlurredLatitude() != null &&
                            r.getBlurredLatitude() > maxLat) ok = false;
                    if (minLon != null && r.getBlurredLongitude() != null &&
                            r.getBlurredLongitude() < minLon) ok = false;
                    if (maxLon != null && r.getBlurredLongitude() != null &&
                            r.getBlurredLongitude() > maxLon) ok = false;

                    if (ok) {
                        Map<String, Object> obj = new HashMap<>();
                        obj.put("reportNumber", r.getReportNumber());
                        obj.put("crimeType", r.getReportNumber());
                        obj.put("description",
                                r.getBlurredAddress() != null ? r.getBlurredAddress() :
                                        (r.getMcppNeighborhood() != null ? r.getMcppNeighborhood() : "No description"));
                        obj.put("latitude", r.getBlurredLatitude());
                        obj.put("longitude", r.getBlurredLongitude());
                        obj.put("precinct", r.getPrecinct());
                        obj.put("sector", r.getSector());
                        obj.put("beat", r.getBeat());
                        obj.put("neighborhood", r.getMcppNeighborhood());
                        obj.put("reportDatetime",
                                r.getReportDatetime() != null ? r.getReportDatetime().toString() : null);

                        results.add(obj);
                    }
                }

                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().print(gson.toJson(results));
                return;
            }


            if ("list".equals(action)) {
                List<CrimeReport> list = dao.findAll();
                List<Map<String, Object>> jsonList = new ArrayList<>();
                
                for (CrimeReport r : list) {
                    Map<String, Object> jsonObj = new HashMap<>();
                    jsonObj.put("reportNumber", r.getReportNumber());
                    jsonObj.put("crimeType", r.getReportNumber()); // Using reportNumber as crimeType for now
                    jsonObj.put("description", r.getBlurredAddress() != null ? r.getBlurredAddress() : 
                                (r.getMcppNeighborhood() != null ? r.getMcppNeighborhood() : "No description"));
                    jsonObj.put("latitude", r.getBlurredLatitude()); // Keep as Double for JSON
                    jsonObj.put("longitude", r.getBlurredLongitude()); // Keep as Double for JSON
                    jsonObj.put("precinct", r.getPrecinct());
                    jsonObj.put("sector", r.getSector());
                    jsonObj.put("beat", r.getBeat());
                    jsonObj.put("neighborhood", r.getMcppNeighborhood());
                    jsonObj.put("reportDatetime", r.getReportDatetime() != null ? r.getReportDatetime().toString() : null);
                    jsonList.add(jsonObj);
                }
                
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.print(gson.toJson(jsonList));
                out.flush();
                return;
            }

            if ("filter".equals(action)) {
                handleFilter(req, resp);
                return;
            }
            
            // Handle JSP page requests (existing functionality)
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
        try {
            // Ensure database is initialized
            DatabaseInitializer.initializeIfNeeded();
            String action = req.getParameter("action");
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
