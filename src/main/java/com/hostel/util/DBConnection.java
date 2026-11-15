package com.hostel.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static final Properties CONFIG = loadConfig();

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = requiredProperty("db.url");
        String user = requiredProperty("db.user");
        String password = CONFIG.getProperty("db.password", "");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL JDBC driver is unavailable.", exception);
        }
        Connection connection = DriverManager.getConnection(url, user, password);
        LOGGER.info("Opened database connection.");
        return connection;
    }

    public static String getAppProperty(String name) {
        return requiredProperty(name);
    }

    private static Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (input == null) {
                throw new IllegalStateException("app.properties was not found on the classpath.");
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static String requiredProperty(String name) {
        String value = CONFIG.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required configuration property is missing: " + name);
        }
        return value.trim();
    }
}
