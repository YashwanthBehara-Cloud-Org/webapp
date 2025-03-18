package com.cloud.webapp.util;

import io.github.cdimascio.dotenv.Dotenv;

public class TestEnvironmentLoader {

    // This method will load environment variables into System properties
    public static void loadEnvironmentVariables() {
        // Check if running in a CI environment
        String isCi = System.getenv("CI");

        if (isCi == null || !isCi.equals("true")) {
            // In Local/Production environment, load .env file using Dotenv
            Dotenv dotenv = Dotenv.load();
            System.setProperty("DB_URL", dotenv.get("DB_URL"));
            System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
            System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        } else {
            // In CI environment (e.g., GitHub Actions), read from system environment variables
            System.setProperty("DB_URL", System.getenv("DB_URL"));
            System.setProperty("DB_USERNAME", System.getenv("DB_USERNAME"));
            System.setProperty("DB_PASSWORD", System.getenv("DB_PASSWORD"));
        }
    }
}
