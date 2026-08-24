package com.example.study11.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayConfigurationTest {

    @Test
    void existingStudy11DatabaseMustBaselineAtVersionZero() throws IOException {
        String applicationYaml = readResource("application.yml");
        String pomXml = readResource("pom.xml");

        assertTrue(applicationYaml.contains("baseline-version: 0"),
                "Spring Flyway must baseline existing study11 at version 0");
        assertTrue(pomXml.contains("<baselineVersion>0</baselineVersion>"),
                "Maven Flyway must baseline existing study11 at version 0");
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream stream = FlywayConfigurationTest.class
                .getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (stream == null) {
                // pom.xml is not a classpath resource; load it from the test working directory.
                return java.nio.file.Files.readString(
                        java.nio.file.Path.of(resourceName), StandardCharsets.UTF_8);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
