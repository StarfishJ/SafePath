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
            if (in == null) {
                throw new RuntimeException("application.properties not found on classpath");
            }

            Properties props = new Properties();
            props.load(in);

            url = props.getProperty("jdbc.url");
            username = props.getProperty("jdbc.username");
            password = props.getProperty("jdbc.password");

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
        } catch (Exception e) {
            System.err.println("ConnectionManager: FAILED to load MySQL configuration");
            e.printStackTrace();
            throw new RuntimeException("Failed to load DB config: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        System.out.println("Connecting to MySQL...");
        Connection conn = DriverManager.getConnection(url, username, password);
        System.out.println("MySQL connection established successfully");
        return conn;
    }
}
