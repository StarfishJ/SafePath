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

@WebServlet(urlPatterns = "/users", loadOnStartup = 1)
public class AppUserServlet extends HttpServlet {
    private final AppUserDao dao = new AppUserDao();

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize database schema if tables don't exist
        try {
            com.safepathjdbc.util.DatabaseInitializer.initializeIfNeeded();
            
            // Test database connection and verify data access
            System.out.println("AppUserServlet.init: Testing database connection...");
            try {
                dao.testDatabaseConnection();
            } catch (SQLException e) {
                System.err.println("AppUserServlet.init: Database test failed: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new ServletException("Failed to initialize database", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Ensure database is initialized (in case init() wasn't called or failed)
            try {
                com.safepathjdbc.util.DatabaseInitializer.initializeIfNeeded();
            } catch (SQLException e) {
                System.err.println("ERROR: Database initialization failed: " + e.getMessage());
                e.printStackTrace();
                throw new ServletException("Database initialization failed. Please check the logs.", e);
            }
            
            String idParam = req.getParameter("id");
            String searchEmail = req.getParameter("searchEmail");
            
            System.out.println("AppUserServlet.doGet - idParam: " + idParam + ", searchEmail: " + searchEmail);
            
            if (idParam != null && !idParam.isEmpty()) {
                // Search by ID
                System.out.println("AppUserServlet: Searching by ID: " + idParam);
                Long id = Long.valueOf(idParam);
                AppUser u = dao.findById(id);
                req.setAttribute("users", u == null ? java.util.List.of() : java.util.List.of(u));
                if (u == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else if (searchEmail != null && !searchEmail.trim().isEmpty()) {
                // Search by email
                String trimmedEmail = searchEmail.trim();
                System.out.println("AppUserServlet: Searching by email: '" + trimmedEmail + "'");
                List<AppUser> users = dao.searchByEmail(trimmedEmail);
                System.out.println("AppUserServlet: Found " + users.size() + " user(s)");
                req.setAttribute("users", users);
                req.setAttribute("searchEmail", trimmedEmail);
            } else {
                // List all
                System.out.println("AppUserServlet: Listing all users");
                List<AppUser> users = dao.findAll();
                System.out.println("AppUserServlet: Found " + users.size() + " user(s) total");
                req.setAttribute("users", users);
            }
            req.getRequestDispatcher("/WEB-INF/jsp/users.jsp").forward(req, resp);
        } catch (SQLException e) {
            System.err.println("AppUserServlet: SQLException: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Database error: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("AppUserServlet: Unexpected exception: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                String email = req.getParameter("email");
                String password = req.getParameter("password");
                AppUser u = new AppUser();
                u.setEmail(email);
                u.setPasswordHash(password);
                dao.create(u);
            } else if ("update".equals(action)) {
                Long id = Long.valueOf(req.getParameter("id"));
                AppUser u = dao.findById(id);
                if (u == null) throw new ServletException("User not found: " + id);
                u.setEmail(req.getParameter("email"));
                String password = req.getParameter("password");
                if (password != null && !password.isEmpty()) {
                    u.setPasswordHash(password);
                }
                dao.update(u);
            } else if ("delete".equals(action)) {
                Long id = Long.valueOf(req.getParameter("id"));
                dao.delete(id);
            }
            resp.sendRedirect(req.getContextPath() + "/users");
        } catch (SQLException e) { throw new ServletException(e); }
    }
}
