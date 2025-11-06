package com.safepathjdbc.dao;

import com.safepathjdbc.model.CrimeReport;
import com.safepathjdbc.util.ConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CrimeReportDao {
    public CrimeReport create(CrimeReport r) throws SQLException {
        String sql = "INSERT INTO crime_report (description, location, reported_at) VALUES (?, ?, ?)";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getDescription());
            ps.setString(2, r.getLocation());
            ps.setTimestamp(3, r.getReportedAt() == null ? Timestamp.valueOf(LocalDateTime.now()) : Timestamp.valueOf(r.getReportedAt()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) r.setId(rs.getLong(1)); }
            return r;
        }
    }

    public CrimeReport findById(Long id) throws SQLException {
        String sql = "SELECT id, description, location, reported_at FROM crime_report WHERE id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CrimeReport(rs.getLong("id"), rs.getString("description"), rs.getString("location"), rs.getTimestamp("reported_at").toLocalDateTime());
                }
                return null;
            }
        }
    }

    public List<CrimeReport> findAll() throws SQLException {
        String sql = "SELECT id, description, location, reported_at FROM crime_report ORDER BY id";
        List<CrimeReport> out = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new CrimeReport(rs.getLong("id"), rs.getString("description"), rs.getString("location"), rs.getTimestamp("reported_at").toLocalDateTime()));
        }
        return out;
    }

    public boolean update(CrimeReport r) throws SQLException {
        String sql = "UPDATE crime_report SET description = ?, location = ?, reported_at = ? WHERE id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getDescription());
            ps.setString(2, r.getLocation());
            ps.setTimestamp(3, Timestamp.valueOf(r.getReportedAt()));
            ps.setLong(4, r.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM crime_report WHERE id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
