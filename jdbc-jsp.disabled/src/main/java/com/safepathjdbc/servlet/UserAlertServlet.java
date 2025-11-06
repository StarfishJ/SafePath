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
            List<UserAlert> list = dao.findAll();
            req.setAttribute("alerts", list);
            req.getRequestDispatcher("/WEB-INF/jsp/userAlerts.jsp").forward(req, resp);
        } catch (SQLException e) { throw new ServletException(e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                UserAlert a = new UserAlert();
                a.setUserId(Long.valueOf(req.getParameter("userId")));
                a.setMessage(req.getParameter("message"));
                dao.create(a);
            } else if ("update".equals(action)) {
                Long id = Long.valueOf(req.getParameter("id"));
                UserAlert a = dao.findById(id);
                a.setUserId(Long.valueOf(req.getParameter("userId")));
                a.setMessage(req.getParameter("message"));
                dao.update(a);
            } else if ("delete".equals(action)) {
                dao.delete(Long.valueOf(req.getParameter("id")));
            }
            resp.sendRedirect(req.getContextPath()+"/alerts");
        } catch (SQLException e) { throw new ServletException(e); }
    }
}
