package com.safepathjdbc.util;

import java.io.File;
import java.io.FileInputStream;
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
        Properties props = new Properties();
        
        // priority 1: read the database configuration from the db.properties file (primary configuration source)
        try {
            File projectRoot = new File(System.getProperty("user.dir"));
            // if running from a subdirectory, need to look up the project root directory
            File dbConfigFile = new File(projectRoot, "db.properties");
            if (!dbConfigFile.exists()) {
                // try to find the project root directory in the parent directory of the current working directory
                dbConfigFile = new File(projectRoot.getParent(), "db.properties");
            }
            if (dbConfigFile.exists()) {
                try (FileInputStream fis = new FileInputStream(dbConfigFile)) {
                    props.load(fis);
                    System.out.println("Loaded database config from: " + dbConfigFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load db.properties from project root: " + e.getMessage());
        }
        
        // priority 2: read the database configuration from the application.properties file (fallback configuration source)
        try (InputStream in = ConnectionManager.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                Properties appProps = new Properties();
                appProps.load(in);
                // if the database configuration is not found in the db.properties file, use the values from the application.properties file
                if (props.getProperty("jdbc.url") == null) {
                    props.setProperty("jdbc.url", appProps.getProperty("jdbc.url", ""));
                }
                if (props.getProperty("jdbc.username") == null) {
                    props.setProperty("jdbc.username", appProps.getProperty("jdbc.username", ""));
                }
                if (props.getProperty("jdbc.password") == null) {
                    props.setProperty("jdbc.password", appProps.getProperty("jdbc.password", ""));
                }
            }
        } catch (Exception e) {
            System.out.println("Could not load application.properties from classpath: " + e.getMessage());
        }
        
        // priority 3: environment variables (highest priority, can override the configuration file)
        url = System.getenv("JDBC_URL");
        if (url == null || url.isEmpty()) {
            url = props.getProperty("jdbc.url");
        }
        
        username = System.getenv("DB_USER");
        if (username == null || username.isEmpty()) {
            username = props.getProperty("jdbc.username");
        }
        
        password = System.getenv("DB_PASSWORD");
        if (password == null || password.isEmpty()) {
            password = props.getProperty("jdbc.password");
        }
        
        if (url == null || url.isEmpty() || username == null || password == null) {
            throw new RuntimeException("Database configuration not found. Please create db.properties in project root.");
        }

        System.out.println("========================================");
        System.out.println("MySQL ConnectionManager loading config:");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);

        // Load MySQL driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver not found", e);
        }

        System.out.println("========================================");
    }

    public static Connection getConnection() throws SQLException {
        System.out.println("Connecting to MySQL...");
        Connection conn = DriverManager.getConnection(url, username, password);
        System.out.println("MySQL connection established successfully");
        return conn;
    }
}
