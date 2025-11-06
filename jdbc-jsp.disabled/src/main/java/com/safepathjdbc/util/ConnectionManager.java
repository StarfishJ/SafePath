package com.safepathjdbc.util;

import java.io.IOException;
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
            } else {
                throw new RuntimeException("application.properties not found on classpath");
            }
            // ensure driver loaded (H2)
            try {
                Class.forName("org.h2.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load DB config", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
