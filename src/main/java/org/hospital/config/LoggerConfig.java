package org.hospital.config;

import java.util.logging.Logger;

public final class LoggerConfig {
    private static final Logger logger = Logger.getLogger(LoggerConfig.class.getName());

    private LoggerConfig() {
    }

    public static void init() {
        System.setProperty("java.util.logging.config.file", "logging.properties");
        logger.info("Logger initialized");
    }
}
