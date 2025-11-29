package org.hospital.config;

import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class LoggerConfig {
    private static final Logger logger = Logger.getLogger(LoggerConfig.class.getName());

    private LoggerConfig() {
    }

    public static void init() {
        try (InputStream is = LoggerConfig.class.getClassLoader()
                .getResourceAsStream("logging.properties")) {
            if (is != null) {
                LogManager.getLogManager().readConfiguration(is);
            }
        } catch (Exception e) {
            System.err.println("Could not load logging.properties: " + e.getMessage());
        }
        logger.info("Logger initialized");
    }
}
