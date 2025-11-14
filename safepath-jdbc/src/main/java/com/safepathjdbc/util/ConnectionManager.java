package com.safepathjdbc.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionManager {
    private static String url;
    private static String username;
    private static String password;

    static {
        try (InputStream in = ConnectionManager.class.getClassLoader().getResourceAsStream("application.properties")) {
            Properties props = new Properties();
            if (in != null) {
                props.load(in);
                url = props.getProperty("jdbc.url");
                username = props.getProperty("jdbc.username");
                password = props.getProperty("jdbc.password");
                
                System.out.println("========================================");
                System.out.println("ConnectionManager: Loading database configuration");
                System.out.println("========================================");
                System.out.println("JDBC URL from properties: " + url);
                System.out.println("Username: " + username);
                
                // Extract and verify database file path
                if (url != null && url.contains("jdbc:h2:file:")) {
                    try {
                        String pathPart = url.substring(url.indexOf("file:") + 5);
                        String params = "";
                        if (pathPart.contains(";")) {
                            int paramStart = pathPart.indexOf(";");
                            params = pathPart.substring(paramStart);
                            pathPart = pathPart.substring(0, paramStart);
                        }
                        
                        System.out.println("Database path from URL: " + pathPart);
                        System.out.println("Current working directory: " + System.getProperty("user.dir"));
                        
                        // Resolve relative path to absolute path
                        java.io.File dbFile;
                        if (pathPart.startsWith("./") || (!pathPart.contains(":") && !pathPart.startsWith("/"))) {
                            // Relative path - resolve from project root
                            // For Jetty/Maven, the working directory is typically the project root
                            String cleanPath = pathPart.replace("./", "").replace(".\\", "");
                            dbFile = new java.io.File(System.getProperty("user.dir"), cleanPath);
                            System.out.println("Resolved relative path from working directory:");
                            System.out.println("  Relative path: " + cleanPath);
                            System.out.println("  Working directory: " + System.getProperty("user.dir"));
                            System.out.println("  Resolved absolute path: " + dbFile.getAbsolutePath());
                        } else {
                            // Absolute path
                            dbFile = new java.io.File(pathPart);
                            System.out.println("Using absolute path: " + dbFile.getAbsolutePath());
                        }
                        
                        // Check if the database file exists
                        java.io.File dbFileMv = new java.io.File(dbFile.getAbsolutePath() + ".mv.db");
                        if (dbFileMv.exists()) {
                            System.out.println("✓ Database file EXISTS: " + dbFileMv.getAbsolutePath());
                            System.out.println("  File size: " + dbFileMv.length() + " bytes");
                            System.out.println("  Last modified: " + new java.util.Date(dbFileMv.lastModified()));
                            
                            // Check if there are other database files in the project
                            java.io.File projectRoot = new java.io.File(System.getProperty("user.dir"));
                            System.out.println("  Checking for other database files in project...");
                            try {
                                java.io.File altDbFile = new java.io.File(projectRoot, "safepath-jdbc/database/safepathdb.mv.db");
                                if (altDbFile.exists() && !altDbFile.getAbsolutePath().equals(dbFileMv.getAbsolutePath())) {
                                    System.out.println("  ⚠ WARNING: Found another database file: " + altDbFile.getAbsolutePath());
                                    System.out.println("    Size: " + altDbFile.length() + " bytes");
                                    System.out.println("    Last modified: " + new java.util.Date(altDbFile.lastModified()));
                                    System.out.println("    This may cause confusion - ensure H2 Console uses the same path!");
                                }
                            } catch (Exception e) {
                                // Ignore
                            }
                            
                            // Always use absolute path to ensure consistency
                            String resolvedUrl = "jdbc:h2:file:" + dbFile.getAbsolutePath().replace("\\", "/") + params;
                            if (!resolvedUrl.equals(url)) {
                                System.out.println("⚠ Updating JDBC URL to use resolved absolute path for consistency");
                                System.out.println("  Original URL: " + url);
                                System.out.println("  Resolved URL: " + resolvedUrl);
                                url = resolvedUrl;
                            }
                        } else {
                            System.out.println("✗ Database file NOT FOUND: " + dbFileMv.getAbsolutePath());
                            System.out.println("  Parent directory: " + dbFileMv.getParentFile().getAbsolutePath());
                            System.out.println("  Parent exists: " + dbFileMv.getParentFile().exists());
                            System.out.println("  Will be created on first connection");
                        }
                        
                        System.out.println("Final JDBC URL: " + url);
                        System.out.println("========================================");
                    } catch (Exception e) {
                        System.err.println("ConnectionManager: Warning - Could not parse database path: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                throw new RuntimeException("application.properties not found on classpath");
            }
            
            // Ensure driver loaded (H2)
            try {
                Class.forName("org.h2.Driver");
                System.out.println("H2 Driver loaded successfully");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("H2 Driver not found", e);
            }
        } catch (Exception e) {
            System.err.println("ConnectionManager: CRITICAL ERROR - Failed to load DB config");
            e.printStackTrace();
            throw new RuntimeException("Failed to load DB config: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        System.out.println("ConnectionManager.getConnection: Creating new connection");
        System.out.println("  URL: " + url);
        System.out.println("  Username: " + username);
        Connection conn = DriverManager.getConnection(url, username, password);
        
        // H2 may have auto-commit disabled by default, ensure it's enabled
        boolean autoCommit = conn.getAutoCommit();
        System.out.println("ConnectionManager.getConnection: Auto-commit: " + autoCommit);
        if (!autoCommit) {
            System.out.println("ConnectionManager.getConnection: WARNING - Auto-commit is disabled! Enabling it...");
            conn.setAutoCommit(true);
        }
        
        System.out.println("ConnectionManager.getConnection: Connection created successfully");
        System.out.println("  Connection URL: " + conn.getMetaData().getURL());
        System.out.println("  Database Product: " + conn.getMetaData().getDatabaseProductName());
        System.out.println("  Database Version: " + conn.getMetaData().getDatabaseProductVersion());
        System.out.println("  Auto-commit: " + conn.getAutoCommit());
        return conn;
    }
}
