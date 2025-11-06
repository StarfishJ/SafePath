package com.safepathjdbc.dao;

import com.safepathjdbc.model.AppUser;
import com.safepathjdbc.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppUserDao {
    public AppUser create(AppUser u) throws SQLException {
        String sql = "INSERT INTO app_user (username, email) VALUES (?, ?)";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getEmail());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setId(rs.getLong(1));
            }
            return u;
        }
    }

    public AppUser findById(Long id) throws SQLException {
        String sql = "SELECT id, username, email FROM app_user WHERE id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return new AppUser(rs.getLong("id"), rs.getString("username"), rs.getString("email"));
                return null;
            }
        }
    }

    public List<AppUser> findAll() throws SQLException {
        String sql = "SELECT id, username, email FROM app_user ORDER BY id";
        List<AppUser> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new AppUser(rs.getLong("id"), rs.getString("username"), rs.getString("email")));
        }
        return list;
    }

    public boolean update(AppUser u) throws SQLException {
        String sql = "UPDATE app_user SET username = ?, email = ? WHERE id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getEmail());
            ps.setLong(3, u.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM app_user WHERE id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
