package com.safepathjdbc.servlet;

import com.safepathjdbc.dao.UserAlertDao;
import com.safepathjdbc.model.UserAlert;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = "/alerts")
public class UserAlertServlet extends HttpServlet {
    private final UserAlertDao dao = new UserAlertDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String idParam = req.getParameter("id");
            String userIdParam = req.getParameter("userId");
            String searchUserId = req.getParameter("searchUserId");
            String searchAlertId = req.getParameter("searchAlertId");
            
            if (idParam != null && !idParam.isEmpty()) {
                // Search by exact alert ID
                Long id = Long.valueOf(idParam);
                UserAlert a = dao.findById(id);
                req.setAttribute("alerts", a == null ? java.util.List.of() : java.util.List.of(a));
                if (a == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if (userIdParam != null && !userIdParam.isEmpty()) {
                // Search by user ID (legacy parameter)
                int userId = Integer.parseInt(userIdParam);
                List<UserAlert> list = dao.findByUserId(userId);
                req.setAttribute("alerts", list);
                req.setAttribute("searchUserId", userIdParam);
            } else if (searchUserId != null && !searchUserId.trim().isEmpty()) {
                // Search by user ID
                try {
                    int userId = Integer.parseInt(searchUserId.trim());
                    List<UserAlert> list = dao.findByUserId(userId);
                    req.setAttribute("alerts", list);
                    req.setAttribute("searchUserId", searchUserId.trim());
                } catch (NumberFormatException e) {
                    req.setAttribute("alerts", java.util.List.of());
                }
            } else if (searchAlertId != null && !searchAlertId.trim().isEmpty()) {
                // Search by alert ID
                try {
                    Long alertId = Long.valueOf(searchAlertId.trim());
                    UserAlert a = dao.findById(alertId);
                    req.setAttribute("alerts", a == null ? java.util.List.of() : java.util.List.of(a));
                    req.setAttribute("searchAlertId", searchAlertId.trim());
                } catch (NumberFormatException e) {
                    req.setAttribute("alerts", java.util.List.of());
                }
            } else {
                // List all
                List<UserAlert> list = dao.findAll();
                req.setAttribute("alerts", list);
            }
            req.getRequestDispatcher("/WEB-INF/jsp/userAlerts.jsp").forward(req, resp);
        } catch (SQLException e) { throw new ServletException(e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                UserAlert a = new UserAlert();
                a.setUserId(Integer.valueOf(req.getParameter("userId")));
                String radius = req.getParameter("radiusM");
                a.setRadiusM(radius == null || radius.isEmpty() ? null : Integer.valueOf(radius));
                String lat = req.getParameter("centerLat");
                a.setCenterLat(lat == null || lat.isEmpty() ? null : Double.valueOf(lat));
                String lon = req.getParameter("centerLon");
                a.setCenterLon(lon == null || lon.isEmpty() ? null : Double.valueOf(lon));
                a.setCrimeTypeFilter(req.getParameter("crimeTypeFilter"));
                String active = req.getParameter("activeFlag");
                a.setActiveFlag(active == null || active.isEmpty() ? null : Boolean.valueOf(active));
                dao.create(a);
            } else if ("update".equals(action)) {
                Long id = Long.valueOf(req.getParameter("id"));
                UserAlert a = dao.findById(id);
                if (a == null) throw new ServletException("User alert not found: " + id);
                a.setUserId(Integer.valueOf(req.getParameter("userId")));
                String radius = req.getParameter("radiusM");
                a.setRadiusM(radius == null || radius.isEmpty() ? null : Integer.valueOf(radius));
                String lat = req.getParameter("centerLat");
                a.setCenterLat(lat == null || lat.isEmpty() ? null : Double.valueOf(lat));
                String lon = req.getParameter("centerLon");
                a.setCenterLon(lon == null || lon.isEmpty() ? null : Double.valueOf(lon));
                a.setCrimeTypeFilter(req.getParameter("crimeTypeFilter"));
                String active = req.getParameter("activeFlag");
                a.setActiveFlag(active == null || active.isEmpty() ? null : Boolean.valueOf(active));
                dao.update(a);
            } else if ("delete".equals(action)) {
                dao.delete(Long.valueOf(req.getParameter("id")));
            }
            resp.sendRedirect(req.getContextPath()+"/alerts");
        } catch (SQLException e) { throw new ServletException(e); }
    }
}
