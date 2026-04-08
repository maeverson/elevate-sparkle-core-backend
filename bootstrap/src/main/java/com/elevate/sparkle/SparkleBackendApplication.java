package com.elevate.sparkle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application
 * Bootstrap module that wires all hexagonal architecture components
 */
@SpringBootApplication(scanBasePackages = "com.elevate.sparkle")
public class SparkleBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SparkleBackendApplication.class, args);
    }
}
