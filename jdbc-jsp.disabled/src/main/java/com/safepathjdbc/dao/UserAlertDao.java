package com.safepathjdbc.dao;

import com.safepathjdbc.model.UserAlert;
import com.safepathjdbc.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAlertDao {
    public UserAlert create(UserAlert a) throws SQLException {
        String sql = "INSERT INTO user_alert (user_id, message) VALUES (?, ?)";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, a.getUserId());
            ps.setString(2, a.getMessage());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) a.setId(rs.getLong(1)); }
            return a;
        }
    }

    public UserAlert findById(Long id) throws SQLException {
        String sql = "SELECT id, user_id, message FROM user_alert WHERE id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new UserAlert(rs.getLong("id"), rs.getLong("user_id"), rs.getString("message"));
                return null;
            }
        }
    }

    public List<UserAlert> findAll() throws SQLException {
        String sql = "SELECT id, user_id, message FROM user_alert ORDER BY id";
        List<UserAlert> out = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new UserAlert(rs.getLong("id"), rs.getLong("user_id"), rs.getString("message")));
        }
        return out;
    }

    public boolean update(UserAlert a) throws SQLException {
        String sql = "UPDATE user_alert SET user_id = ?, message = ? WHERE id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, a.getUserId());
            ps.setString(2, a.getMessage());
            ps.setLong(3, a.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM user_alert WHERE id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
