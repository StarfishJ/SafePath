package com.safepathjdbc.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static volatile boolean initialized = false;
    private static final Object lock = new Object();

    public static void initializeIfNeeded() throws SQLException {
        if (initialized) {
            return;
        }

        synchronized (lock) {
            if (initialized) {
                return;
            }

            try (Connection conn = ConnectionManager.getConnection()) {
                System.out.println("DatabaseInitializer: Checking if users table exists...");
                System.out.println("DatabaseInitializer: Connected to database");
                
                // Check if users table exists - use direct query method as it's more reliable
                boolean tableExists = false;
                try (java.sql.Statement stmt = conn.createStatement()) {
                    try {
                        // Try to query the table - this is the most reliable way to check
                        java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                        if (rs.next()) {
                            int count = rs.getInt(1);
                            tableExists = true;
                            System.out.println("DatabaseInitializer: ✓ users table exists with " + count + " row(s)");
                        }
                    } catch (SQLException e) {
                        // Check error code - different codes for different databases
                        int errorCode = e.getErrorCode();
                        String errorMsg = e.getMessage();
                        String sqlState = e.getSQLState();
                        
                        // MySQL error code 1146 = Table doesn't exist
                        // H2 error code 42104 = Table doesn't exist
                        // SQLState 42S02 = Base table or view not found (standard SQL)
                        boolean tableNotFound = (errorCode == 1146) || // MySQL
                                               (errorCode == 42104) || // H2
                                               ("42S02".equals(sqlState)) || // Standard SQL
                                               (errorMsg != null && (
                                                   errorMsg.toLowerCase().contains("table") && 
                                                   (errorMsg.toLowerCase().contains("doesn't exist") ||
                                                    errorMsg.toLowerCase().contains("does not exist") ||
                                                    errorMsg.toLowerCase().contains("not found"))
                                               ));
                        
                        if (tableNotFound) {
                            tableExists = false;
                            System.out.println("DatabaseInitializer: ✗ users table does NOT exist (error code: " + errorCode + ", SQLState: " + sqlState + ")");
                        } else {
                            // Other error - log it but assume table doesn't exist
                            System.err.println("DatabaseInitializer: Unexpected error checking table:");
                            System.err.println("  Error code: " + errorCode);
                            System.err.println("  SQLState: " + sqlState);
                            System.err.println("  Message: " + errorMsg);
                            tableExists = false;
                        }
                    }
                }
                
                if (!tableExists) {
                    // Tables don't exist, initialize schema
                    System.out.println("DatabaseInitializer: Database is empty. Initializing schema...");
                    
                    // Try to execute full script first
                    boolean scriptSuccess = false;
                    try {
                        executeInitScript(conn);
                        scriptSuccess = true;
                        System.out.println("DatabaseInitializer: Schema initialization from script completed.");
                    } catch (SQLException e) {
                        System.err.println("DatabaseInitializer: Script execution failed: " + e.getMessage());
                        System.err.println("DatabaseInitializer: Falling back to direct table creation...");
                    }
                    
                    // If script failed or didn't create all tables, create core tables directly
                    if (!scriptSuccess) {
                        try {
                            createUsersTableDirectly(conn);
                            System.out.println("DatabaseInitializer: Created core tables directly");
                        } catch (SQLException e) {
                            System.err.println("DatabaseInitializer: Direct table creation also failed: " + e.getMessage());
                            throw new SQLException("Failed to create database tables. Please check database configuration.", e);
                        }
                    }
                    
                    // Verify that users table was created
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        stmt.executeQuery("SELECT 1 FROM users LIMIT 1");
                        System.out.println("DatabaseInitializer: Verification passed - users table is accessible");
                    } catch (SQLException e) {
                        System.err.println("DatabaseInitializer: CRITICAL - Verification failed after initialization!");
                        System.err.println("Error: " + e.getMessage());
                        System.err.println("Error code: " + e.getErrorCode());
                        throw new SQLException("Table creation verification failed. Users table is not accessible.", e);
                    }
                } else {
                    System.out.println("DatabaseInitializer: Database tables already exist. Skipping initialization.");
                }
                
                initialized = true;
            } catch (Exception e) {
                System.err.println("DatabaseInitializer: Error initializing database: " + e.getMessage());
                e.printStackTrace();
                initialized = false; // Reset flag so we can retry
                throw new SQLException("Failed to initialize database: " + e.getMessage(), e);
            }
        }
    }
    
    private static void createUsersTableDirectly(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Create users table
            String usersSql = "CREATE TABLE IF NOT EXISTS users (" +
                             "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                             "email VARCHAR(255) NOT NULL UNIQUE, " +
                             "password_hash VARCHAR(255) NOT NULL, " +
                             "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                             ")";
            stmt.execute(usersSql);
            System.out.println("DatabaseInitializer: Created users table");
            
            // Create crime_reports table
            String crimeReportsSql = "CREATE TABLE IF NOT EXISTS crime_reports (" +
                                    "report_number VARCHAR(50) PRIMARY KEY, " +
                                    "report_datetime DATETIME, " +
                                    "precinct VARCHAR(100), " +
                                    "sector VARCHAR(10), " +
                                    "beat VARCHAR(10), " +
                                    "mcpp_neighborhood VARCHAR(100), " +
                                    "blurred_address VARCHAR(255), " +
                                    "blurred_latitude DOUBLE, " +
                                    "blurred_longitude DOUBLE" +
                                    ")";
            stmt.execute(crimeReportsSql);
            System.out.println("DatabaseInitializer: Created crime_reports table");
            
            // Create user_alerts table
            String userAlertsSql = "CREATE TABLE IF NOT EXISTS user_alerts (" +
                                  "alert_id INT AUTO_INCREMENT PRIMARY KEY, " +
                                  "user_id INT NOT NULL, " +
                                  "radius_m INT, " +
                                  "center_lat DOUBLE, " +
                                  "center_lon DOUBLE, " +
                                  "crime_type_filter VARCHAR(100), " +
                                  "active_flag BOOLEAN DEFAULT TRUE, " +
                                  "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                  "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                                  "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                  ")";
            stmt.execute(userAlertsSql);
            System.out.println("DatabaseInitializer: Created user_alerts table");
        }
    }

    private static void executeInitScript(Connection conn) throws SQLException {
        InputStream is = DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream("init_schema_if_not_exists.sql");
        
        if (is == null) {
            throw new SQLException("Cannot find init_schema_if_not_exists.sql in classpath");
        }

        System.out.println("DatabaseInitializer: Loading SQL script from classpath...");
        
        try {
            // Read script line by line, build statements
            java.util.List<String> statements = new java.util.ArrayList<>();
            StringBuilder currentStatement = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    
                    // Skip empty lines and comments
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                        continue;
                    }
                    
                    currentStatement.append(line).append(" ");
                    
                    // If line ends with semicolon, we have a complete statement
                    if (trimmed.endsWith(";")) {
                        String stmt = currentStatement.toString().trim();
                        // Remove trailing semicolon
                        if (stmt.endsWith(";")) {
                            stmt = stmt.substring(0, stmt.length() - 1).trim();
                        }
                        if (!stmt.isEmpty()) {
                            statements.add(stmt);
                        }
                        currentStatement.setLength(0);
                    }
                }
                
                // Add any remaining statement
                if (currentStatement.length() > 0) {
                    String stmt = currentStatement.toString().trim();
                    if (stmt.endsWith(";")) {
                        stmt = stmt.substring(0, stmt.length() - 1).trim();
                    }
                    if (!stmt.isEmpty()) {
                        statements.add(stmt);
                    }
                }
            }
            
            System.out.println("DatabaseInitializer: Found " + statements.size() + " SQL statements to execute");
            
            // Execute each statement
            try (Statement stmt = conn.createStatement()) {
                int executedCount = 0;
                for (String sql : statements) {
                    sql = sql.trim();
                    if (sql.isEmpty()) {
                        continue;
                    }
                    
                    try {
                        // Extract table name for logging
                        String tableName = "unknown";
                        String upperSql = sql.toUpperCase();
                        if (upperSql.contains("CREATE TABLE")) {
                            // Find CREATE TABLE [IF NOT EXISTS] table_name
                            int createIdx = upperSql.indexOf("CREATE TABLE");
                            int start = createIdx + 12; // After "CREATE TABLE"
                            // Skip "IF NOT EXISTS" if present
                            String afterCreate = sql.substring(start).trim();
                            if (afterCreate.toUpperCase().startsWith("IF NOT EXISTS")) {
                                start = sql.toUpperCase().indexOf("IF NOT EXISTS", start) + 13;
                            }
                            // Find the table name (up to opening parenthesis or space)
                            String rest = sql.substring(start).trim();
                            int spaceIdx = rest.indexOf(' ');
                            int parenIdx = rest.indexOf('(');
                            int end = (parenIdx > 0 && parenIdx < spaceIdx) ? parenIdx : 
                                      (spaceIdx > 0 ? spaceIdx : parenIdx > 0 ? parenIdx : rest.length());
                            if (end > 0) {
                                tableName = rest.substring(0, end).trim();
                            }
                        }
                        
                        System.out.println("DatabaseInitializer: Executing CREATE TABLE for: " + tableName);
                        stmt.execute(sql);
                        executedCount++;
                        System.out.println("DatabaseInitializer: Successfully created table: " + tableName);
                    } catch (SQLException e) {
                        String errorMsg = e.getMessage();
                        if (errorMsg != null) {
                            String upperMsg = errorMsg.toUpperCase();
                            // Ignore "table already exists" - this is expected with IF NOT EXISTS
                            if (upperMsg.contains("ALREADY EXISTS") || upperMsg.contains("DUPLICATE")) {
                                System.out.println("DatabaseInitializer: Table already exists (expected)");
                                executedCount++;
                            } else {
                                System.err.println("DatabaseInitializer: SQL Error executing statement:");
                                System.err.println("  Error: " + errorMsg);
                                System.err.println("  SQL: " + sql.substring(0, Math.min(150, sql.length())));
                                // For foreign key errors, log but continue (tables created in order)
                                if (upperMsg.contains("REFERENCE") || upperMsg.contains("FOREIGN KEY")) {
                                    System.err.println("  (Foreign key error - may be due to table creation order, continuing...)");
                                } else {
                                    // For other errors, throw exception
                                    throw new SQLException("Failed to execute SQL: " + errorMsg + "\nSQL: " + sql.substring(0, Math.min(200, sql.length())), e);
                                }
                            }
                        } else {
                            throw e;
                        }
                    }
                }
                System.out.println("DatabaseInitializer: Executed " + executedCount + " statements successfully");
            }
            
            // Verify users table was created
            System.out.println("DatabaseInitializer: Verifying users table...");
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                if (rs.next()) {
                    System.out.println("DatabaseInitializer: Verification successful - users table exists and is accessible");
                }
            } catch (SQLException e) {
                System.err.println("DatabaseInitializer: CRITICAL - Verification failed!");
                System.err.println("  Error: " + e.getMessage());
                System.err.println("  Error code: " + e.getErrorCode());
                throw new SQLException("Users table verification failed after initialization. Table may not have been created properly.", e);
            }
        } catch (Exception e) {
            System.err.println("DatabaseInitializer: Exception in executeInitScript: " + e.getMessage());
            e.printStackTrace();
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw new SQLException("Failed to execute initialization script: " + e.getMessage(), e);
        }
    }
}

