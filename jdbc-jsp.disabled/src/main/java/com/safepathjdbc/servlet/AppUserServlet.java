package com.safepathjdbc.servlet;

import com.safepathjdbc.dao.AppUserDao;
import com.safepathjdbc.model.AppUser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = "/users")
public class AppUserServlet extends HttpServlet {
    private final AppUserDao dao = new AppUserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<AppUser> users = dao.findAll();
            req.setAttribute("users", users);
            req.getRequestDispatcher("/WEB-INF/jsp/users.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                String username = req.getParameter("username");
                String email = req.getParameter("email");
                AppUser u = new AppUser(); u.setUsername(username); u.setEmail(email);
                dao.create(u);
            } else if ("update".equals(action)) {
                Long id = Long.valueOf(req.getParameter("id"));
                AppUser u = dao.findById(id);
                u.setUsername(req.getParameter("username"));
                u.setEmail(req.getParameter("email"));
                dao.update(u);
            } else if ("delete".equals(action)) {
                Long id = Long.valueOf(req.getParameter("id"));
                dao.delete(id);
            }
            resp.sendRedirect(req.getContextPath() + "/users");
        } catch (SQLException e) { throw new ServletException(e); }
    }
}
