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
}
