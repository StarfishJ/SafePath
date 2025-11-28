package com.safepathjdbc.dao;

import com.safepathjdbc.model.UserAlert;
import com.safepathjdbc.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAlertDao {
    public UserAlert create(UserAlert a) throws SQLException {
        String sql = "INSERT INTO user_alerts (user_id, radius_m, center_lat, center_lon, crime_type_filter, active_flag) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getUserId());
            if (a.getRadiusM() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, a.getRadiusM());
            if (a.getCenterLat() == null) ps.setNull(3, Types.DOUBLE); else ps.setDouble(3, a.getCenterLat());
            if (a.getCenterLon() == null) ps.setNull(4, Types.DOUBLE); else ps.setDouble(4, a.getCenterLon());
            ps.setString(5, a.getCrimeTypeFilter());
            if (a.getActiveFlag() == null) ps.setNull(6, Types.BOOLEAN); else ps.setBoolean(6, a.getActiveFlag());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) a.setAlertId(rs.getInt(1)); }
            return a;
        }
    }

    public UserAlert findById(Long id) throws SQLException {
        String sql = "SELECT alert_id, user_id, radius_m, center_lat, center_lon, crime_type_filter, active_flag, created_at, updated_at FROM user_alerts WHERE alert_id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserAlert a = new UserAlert();
                    a.setAlertId(rs.getInt("alert_id"));
                    a.setUserId(rs.getInt("user_id"));
                    int radius = rs.getInt("radius_m"); a.setRadiusM(rs.wasNull() ? null : radius);
                    double lat = rs.getDouble("center_lat"); a.setCenterLat(rs.wasNull() ? null : lat);
                    double lon = rs.getDouble("center_lon"); a.setCenterLon(rs.wasNull() ? null : lon);
                    a.setCrimeTypeFilter(rs.getString("crime_type_filter"));
                    boolean active = rs.getBoolean("active_flag"); a.setActiveFlag(rs.wasNull() ? null : active);
                    Timestamp cAt = rs.getTimestamp("created_at"); a.setCreatedAt(cAt == null ? null : cAt.toLocalDateTime());
                    Timestamp uAt = rs.getTimestamp("updated_at"); a.setUpdatedAt(uAt == null ? null : uAt.toLocalDateTime());
                    return a;
                }
                return null;
            }
        }
    }

    public List<UserAlert> findAll() throws SQLException {
        String sql = "SELECT alert_id, user_id, radius_m, center_lat, center_lon, crime_type_filter, active_flag, created_at, updated_at FROM user_alerts ORDER BY alert_id";
        List<UserAlert> out = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UserAlert a = new UserAlert();
                a.setAlertId(rs.getInt("alert_id"));
                a.setUserId(rs.getInt("user_id"));
                int radius = rs.getInt("radius_m"); a.setRadiusM(rs.wasNull() ? null : radius);
                double lat = rs.getDouble("center_lat"); a.setCenterLat(rs.wasNull() ? null : lat);
                double lon = rs.getDouble("center_lon"); a.setCenterLon(rs.wasNull() ? null : lon);
                a.setCrimeTypeFilter(rs.getString("crime_type_filter"));
                boolean active = rs.getBoolean("active_flag"); a.setActiveFlag(rs.wasNull() ? null : active);
                Timestamp cAt = rs.getTimestamp("created_at"); a.setCreatedAt(cAt == null ? null : cAt.toLocalDateTime());
                Timestamp uAt = rs.getTimestamp("updated_at"); a.setUpdatedAt(uAt == null ? null : uAt.toLocalDateTime());
                out.add(a);
            }
        }
        return out;
    }

    public List<UserAlert> findByUserId(int userId) throws SQLException {
        String sql = "SELECT alert_id, user_id, radius_m, center_lat, center_lon, crime_type_filter, active_flag, created_at, updated_at FROM user_alerts WHERE user_id = ? ORDER BY alert_id";
        List<UserAlert> out = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UserAlert a = new UserAlert();
                    a.setAlertId(rs.getInt("alert_id"));
                    a.setUserId(rs.getInt("user_id"));
                    int radius = rs.getInt("radius_m"); a.setRadiusM(rs.wasNull() ? null : radius);
                    double lat = rs.getDouble("center_lat"); a.setCenterLat(rs.wasNull() ? null : lat);
                    double lon = rs.getDouble("center_lon"); a.setCenterLon(rs.wasNull() ? null : lon);
                    a.setCrimeTypeFilter(rs.getString("crime_type_filter"));
                    boolean active = rs.getBoolean("active_flag"); a.setActiveFlag(rs.wasNull() ? null : active);
                    Timestamp cAt = rs.getTimestamp("created_at"); a.setCreatedAt(cAt == null ? null : cAt.toLocalDateTime());
                    Timestamp uAt = rs.getTimestamp("updated_at"); a.setUpdatedAt(uAt == null ? null : uAt.toLocalDateTime());
                    out.add(a);
                }
            }
        }
        return out;
    }

    public boolean update(UserAlert a) throws SQLException {
        String sql = "UPDATE user_alerts SET user_id = ?, radius_m = ?, center_lat = ?, center_lon = ?, crime_type_filter = ?, active_flag = ? WHERE alert_id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a.getUserId());
            if (a.getRadiusM() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, a.getRadiusM());
            if (a.getCenterLat() == null) ps.setNull(3, Types.DOUBLE); else ps.setDouble(3, a.getCenterLat());
            if (a.getCenterLon() == null) ps.setNull(4, Types.DOUBLE); else ps.setDouble(4, a.getCenterLon());
            ps.setString(5, a.getCrimeTypeFilter());
            if (a.getActiveFlag() == null) ps.setNull(6, Types.BOOLEAN); else ps.setBoolean(6, a.getActiveFlag());
            ps.setInt(7, a.getAlertId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM user_alerts WHERE alert_id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
