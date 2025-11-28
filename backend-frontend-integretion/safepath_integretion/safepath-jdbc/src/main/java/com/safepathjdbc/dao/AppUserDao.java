package com.safepathjdbc.dao;

import com.safepathjdbc.model.AppUser;
import com.safepathjdbc.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppUserDao {
    public AppUser create(AppUser u) throws SQLException {
        String sql = "INSERT INTO users (email, password_hash) VALUES (?, ?)";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getPasswordHash());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setUserId(rs.getInt(1));
            }
            return u;
        }
    }

    public AppUser findById(Long id) throws SQLException {
        String sql = "SELECT user_id, email, password_hash, created_at FROM users WHERE user_id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    return new AppUser(
                        rs.getInt("user_id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        ts == null ? null : ts.toLocalDateTime()
                    );
                }
                return null;
            }
        }
    }

    public List<AppUser> findAll() throws SQLException {
        String sql = "SELECT user_id, email, password_hash, created_at FROM users ORDER BY user_id";
        System.out.println("========================================");
        System.out.println("AppUserDao.findAll: Starting query");
        System.out.println("========================================");
        System.out.println("SQL: " + sql);
        
        List<AppUser> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection()) {
            System.out.println("AppUserDao.findAll: Got connection");
            System.out.println("  Connection URL: " + c.getMetaData().getURL());
            
            // First, check if table exists and count rows
            try (Statement checkStmt = c.createStatement();
                 ResultSet countRs = checkStmt.executeQuery("SELECT COUNT(*) as total FROM users")) {
                if (countRs.next()) {
                    int totalCount = countRs.getInt("total");
                    System.out.println("AppUserDao.findAll: Total users in database: " + totalCount);
                }
            } catch (SQLException e) {
                System.err.println("AppUserDao.findAll: Error counting users: " + e.getMessage());
                System.err.println("  Error code: " + e.getErrorCode());
                System.err.println("  SQL state: " + e.getSQLState());
            }
            
            try (PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                System.out.println("AppUserDao.findAll: Query executed, processing results...");
                int rowNum = 0;
                while (rs.next()) {
                    rowNum++;
                    Timestamp ts = rs.getTimestamp("created_at");
                    AppUser user = new AppUser(
                        rs.getInt("user_id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        ts == null ? null : ts.toLocalDateTime()
                    );
                    list.add(user);
                    System.out.println("  User #" + rowNum + ": ID=" + user.getUserId() + ", Email='" + user.getEmail() + "'");
                }
                System.out.println("AppUserDao.findAll: Processed " + rowNum + " rows");
            }
        }
        System.out.println("AppUserDao.findAll: Returning " + list.size() + " users");
        System.out.println("========================================");
        return list;
    }

    public boolean update(AppUser u) throws SQLException {
        String sql = "UPDATE users SET email = ?, password_hash = ? WHERE user_id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getPasswordHash());
            ps.setInt(3, u.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(Long id) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<AppUser> searchByEmail(String emailPattern) throws SQLException {
        if (emailPattern == null || emailPattern.trim().isEmpty()) {
            System.out.println("AppUserDao.searchByEmail: emailPattern is null or empty, returning all users");
            return findAll();
        }
        
        // First, let's check what's actually in the database
        System.out.println("AppUserDao.searchByEmail: Checking database contents first...");
        try (Connection c = ConnectionManager.getConnection()) {
            try (Statement stmt = c.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT user_id, email FROM users")) {
                System.out.println("AppUserDao.searchByEmail: All users in database:");
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("  User " + count + ": ID=" + rs.getInt("user_id") + ", Email='" + rs.getString("email") + "'");
                }
                System.out.println("AppUserDao.searchByEmail: Total users in database: " + count);
            }
        }
        
        // Now perform the search
        // Use LOWER() for case-insensitive search (H2 in MySQL mode may be case-sensitive)
        String trimmedPattern = emailPattern.trim();
        String sql = "SELECT user_id, email, password_hash, created_at FROM users WHERE LOWER(email) LIKE LOWER(?) ORDER BY user_id";
        String searchPattern = "%" + trimmedPattern + "%";
        System.out.println("AppUserDao.searchByEmail: Searching for pattern: '" + trimmedPattern + "'");
        System.out.println("AppUserDao.searchByEmail: SQL pattern will be: '" + searchPattern + "'");
        System.out.println("AppUserDao.searchByEmail: Executing query: " + sql);
        System.out.println("AppUserDao.searchByEmail: Using case-insensitive search with LOWER()");
        
        List<AppUser> list = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, searchPattern);
            System.out.println("AppUserDao.searchByEmail: PreparedStatement parameter set to: '" + searchPattern + "'");
            
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("AppUserDao.searchByEmail: Query executed, processing results...");
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    Timestamp ts = rs.getTimestamp("created_at");
                    String email = rs.getString("email");
                    int userId = rs.getInt("user_id");
                    System.out.println("AppUserDao.searchByEmail: Row " + rowCount + " - ID=" + userId + ", Email='" + email + "'");
                    
                    AppUser user = new AppUser(
                        userId,
                        email,
                        rs.getString("password_hash"),
                        ts == null ? null : ts.toLocalDateTime()
                    );
                    list.add(user);
                    System.out.println("AppUserDao.searchByEmail: Added user to result list: " + user.getEmail());
                }
                System.out.println("AppUserDao.searchByEmail: Processed " + rowCount + " rows from ResultSet");
            }
        } catch (SQLException e) {
            System.err.println("AppUserDao.searchByEmail: SQLException occurred: " + e.getMessage());
            System.err.println("AppUserDao.searchByEmail: Error code: " + e.getErrorCode());
            System.err.println("AppUserDao.searchByEmail: SQL state: " + e.getSQLState());
            e.printStackTrace();
            throw e;
        }
        
        System.out.println("AppUserDao.searchByEmail: Final result - Total found: " + list.size());
        return list;
    }
    
    // Test method to verify database connection and insert test data
    public void testDatabaseConnection() throws SQLException {
        System.out.println("========================================");
        System.out.println("AppUserDao.testDatabaseConnection: Testing database connection");
        System.out.println("========================================");
        
        try (Connection c = ConnectionManager.getConnection()) {
            System.out.println("✓ Got connection");
            System.out.println("  Connection URL: " + c.getMetaData().getURL());
            
            // Check if table exists
            try (Statement stmt = c.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM users")) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    System.out.println("✓ Table 'users' exists with " + total + " row(s)");
                }
            }
            
            // Try to insert a test user directly
            System.out.println("Attempting to insert test user...");
            try (Statement stmt = c.createStatement()) {
                // First, check if bob@gmail.com already exists
                try (ResultSet rs = stmt.executeQuery("SELECT user_id, email FROM users WHERE email = 'bob@gmail.com'")) {
                    if (rs.next()) {
                        System.out.println("⚠ User 'bob@gmail.com' already exists with ID: " + rs.getInt("user_id"));
                    } else {
                        System.out.println("✗ User 'bob@gmail.com' NOT found in database");
                        System.out.println("  Attempting to insert test user...");
                        int rows = stmt.executeUpdate(
                            "INSERT INTO users (email, password_hash) VALUES ('bob@gmail.com', 'test_hash')"
                        );
                        System.out.println("  Insert result: " + rows + " row(s) affected");
                        System.out.println("  Auto-commit enabled: " + c.getAutoCommit());
                        if (!c.getAutoCommit()) {
                            c.commit();
                            System.out.println("  Manually committed transaction");
                        }
                        
                        // Verify insertion
                        try (ResultSet verifyRs = stmt.executeQuery("SELECT user_id, email FROM users WHERE email = 'bob@gmail.com'")) {
                            if (verifyRs.next()) {
                                System.out.println("✓ Successfully inserted and verified user: ID=" + verifyRs.getInt("user_id") + ", Email=" + verifyRs.getString("email"));
                            } else {
                                System.err.println("✗ CRITICAL: User was inserted but cannot be found!");
                            }
                        }
                    }
                }
            }
            
            // List all users
            System.out.println("\nAll users in database:");
            try (Statement stmt = c.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT user_id, email FROM users ORDER BY user_id")) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.println("  User " + count + ": ID=" + rs.getInt("user_id") + ", Email='" + rs.getString("email") + "'");
                }
                if (count == 0) {
                    System.out.println("  (no users found)");
                }
            }
        }
        System.out.println("========================================");
    }
}
