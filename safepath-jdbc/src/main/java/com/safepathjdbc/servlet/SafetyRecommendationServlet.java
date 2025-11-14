package com.safepathjdbc.servlet;

import com.safepathjdbc.dao.CrimeReportDao;
import com.safepathjdbc.dao.UserAlertDao;
import com.safepathjdbc.model.CrimeReport;
import com.safepathjdbc.model.UserAlert;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = "/safety")
public class SafetyRecommendationServlet extends HttpServlet {
    private final CrimeReportDao crimeDao = new CrimeReportDao();
    private final UserAlertDao alertDao = new UserAlertDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String userIdParam = req.getParameter("userId");
            String latParam = req.getParameter("centerLat");
            String lonParam = req.getParameter("centerLon");
            String radiusParam = req.getParameter("radiusM");
            String daysParam = req.getParameter("days");

            // Prefill from user's first alert if only userId is provided
            if ((latParam == null || lonParam == null || radiusParam == null) && userIdParam != null && !userIdParam.isEmpty()) {
                try {
                    int uid = Integer.parseInt(userIdParam);
                    List<UserAlert> alerts = alertDao.findByUserId(uid);
                    if (!alerts.isEmpty()) {
                        UserAlert a = alerts.get(0);
                        if (latParam == null && a.getCenterLat() != null) latParam = a.getCenterLat().toString();
                        if (lonParam == null && a.getCenterLon() != null) lonParam = a.getCenterLon().toString();
                        if (radiusParam == null && a.getRadiusM() != null) radiusParam = a.getRadiusM().toString();
                    }
                } catch (NumberFormatException ignore) {}
            }

            Double lat = parseDouble(latParam);
            Double lon = parseDouble(lonParam);
            Integer radius = parseInt(radiusParam);
            Integer days = parseInt(daysParam);

            List<CrimeReport> reports = crimeDao.searchByGeoAndTime(lat, lon, radius, days == null ? 30 : days);
            req.setAttribute("reports", reports);
            req.setAttribute("centerLat", latParam);
            req.setAttribute("centerLon", lonParam);
            req.setAttribute("radiusM", radiusParam);
            req.setAttribute("days", days == null ? "30" : days.toString());
            req.setAttribute("userId", userIdParam);

            // Simple insights
            Map<String, Long> byNeighborhood = reports.stream()
                .filter(r -> r.getMcppNeighborhood() != null && !r.getMcppNeighborhood().isBlank())
                .collect(Collectors.groupingBy(CrimeReport::getMcppNeighborhood, Collectors.counting()));
            List<Map.Entry<String, Long>> topNeighborhoods = byNeighborhood.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5).collect(Collectors.toList());
            req.setAttribute("topNeighborhoods", topNeighborhoods);
            req.setAttribute("totalCount", reports.size());

            // Recommendation text (very simple heuristic)
            String recommendation = buildRecommendation(reports, topNeighborhoods);
            req.setAttribute("recommendation", recommendation);

            req.getRequestDispatcher("/WEB-INF/jsp/safety.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private static Double parseDouble(String s) {
        try { return s == null || s.isBlank() ? null : Double.valueOf(s.trim()); } catch (Exception e) { return null; }
    }
    private static Integer parseInt(String s) {
        try { return s == null || s.isBlank() ? null : Integer.valueOf(s.trim()); } catch (Exception e) { return null; }
    }

    private static String buildRecommendation(List<CrimeReport> reports, List<Map.Entry<String, Long>> topNeighborhoods) {
        if (reports == null || reports.isEmpty()) {
            return "No recent incidents in the selected area and time window. This area appears low risk.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(reports.size()).append(" incidents nearby. ");
        if (!topNeighborhoods.isEmpty()) {
            sb.append("Higher activity in: ");
            for (int i = 0; i < topNeighborhoods.size(); i++) {
                Map.Entry<String, Long> e = topNeighborhoods.get(i);
                if (i > 0) sb.append(", ");
                sb.append(e.getKey()).append(" (").append(e.getValue()).append(")");
            }
            sb.append(". ");
        }
        sb.append("Recommendation: avoid peak areas above, prefer routes along better-lit main roads, and consider widening distance from hotspots by 1-2 blocks.");
        return sb.toString();
    }
}


