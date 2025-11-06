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
import java.util.List;

@WebServlet(urlPatterns = "/crimeReports")
public class CrimeReportServlet extends HttpServlet {
    private final CrimeReportDao dao = new CrimeReportDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<CrimeReport> list = dao.findAll();
            req.setAttribute("reports", list);
            req.getRequestDispatcher("/WEB-INF/jsp/crimeReports.jsp").forward(req, resp);
        } catch (SQLException e) { throw new ServletException(e); }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            if ("create".equals(action)) {
                CrimeReport r = new CrimeReport();
                r.setDescription(req.getParameter("description"));
                r.setLocation(req.getParameter("location"));
                r.setReportedAt(LocalDateTime.now());
                dao.create(r);
            } else if ("update".equals(action)) {
                Long id = Long.valueOf(req.getParameter("id"));
                CrimeReport r = dao.findById(id);
                r.setDescription(req.getParameter("description"));
                r.setLocation(req.getParameter("location"));
                r.setReportedAt(LocalDateTime.parse(req.getParameter("reportedAt")));
                dao.update(r);
            } else if ("delete".equals(action)) {
                dao.delete(Long.valueOf(req.getParameter("id")));
            }
            resp.sendRedirect(req.getContextPath()+"/crimeReports");
        } catch (SQLException e) { throw new ServletException(e); }
    }
}
