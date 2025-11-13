package org.hospital.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Database configuration using basic JDBC DriverManager.
 * V1: Simple connection management without pooling.
 * V2 will add: Connection pooling (HikariCP) for better performance.
 */
public final class DatabaseConfig {
    private static final Logger logger = Logger.getLogger(DatabaseConfig.class.getName());
    private static final Properties properties = loadProperties();

    static {
        validateRequiredProperty("db.url");
        validateRequiredProperty("db.username");
        validateRequiredProperty("db.password");
        
        // Load Oracle JDBC driver
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            logger.info("Oracle JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Oracle JDBC Driver not found", e);
        }
    }

    private DatabaseConfig() {
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("application.properties file not found in classpath");
            }
            props.load(input);
            logger.info("Successfully loaded application.properties");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load application.properties", e);
        }
        return props;
    }

    private static void validateRequiredProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    String.format("Required property '%s' not configured in application.properties", key));
        }
    }

    /**
     * Get a database connection using DriverManager.
     * V1: Basic JDBC connection (no pooling).
     * V2 will use: Connection pooling for better performance.
     * 
     * @return A new database connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        
        logger.fine("Creating new database connection");
        return DriverManager.getConnection(url, username, password);
    }
}
