package com.example.study11.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageConfigurationTest {

    @Test
    void resumeStorageDefaultsAreDeclaredInApplicationYaml() throws IOException {
        String applicationYaml = readApplicationYaml();

        assertTrue(applicationYaml.contains("  servlet:\n    multipart:\n"
                        + "      max-file-size: ${FILE_STORAGE_MAX_FILE_SIZE:10MB}\n"
                        + "      max-request-size: ${FILE_STORAGE_MAX_REQUEST_SIZE:11MB}"),
                "Multipart limits must be declared under spring.servlet.multipart");
        assertTrue(applicationYaml.contains("file:\n  storage:"),
                "Resume storage configuration must live under file.storage");
        assertTrue(applicationYaml.contains("upload-dir: ${FILE_STORAGE_UPLOAD_DIR:./data/recruitment-resumes}"),
                "Resume uploads must use an overridable local storage root");
        assertTrue(applicationYaml.contains("max-file-size: ${FILE_STORAGE_MAX_FILE_SIZE:10MB}"),
                "Resume uploads must default to a 10MB size limit");
        assertTrue(applicationYaml.contains("allowed-extensions:\n      - pdf\n      - doc\n      - docx"),
                "Resume uploads must default to PDF, DOC, and DOCX only");
        assertTrue(applicationYaml.contains("max-request-size: ${FILE_STORAGE_MAX_REQUEST_SIZE:11MB}"),
                "Multipart request size must follow the resume storage limit");
        assertTrue(applicationYaml.contains("  photo:\n    upload-dir: ${FILE_PHOTO_UPLOAD_DIR:./data/employee-photos}"),
                "Photo uploads must use an overridable local storage root");
        assertTrue(applicationYaml.contains("max-file-size: ${FILE_PHOTO_MAX_FILE_SIZE:2MB}"),
                "Photo uploads must default to a 2MB size limit");
        assertTrue(applicationYaml.contains("allowed-extensions:\n      - jpg\n      - jpeg\n      - png"),
                "Photo uploads must default to JPG, JPEG, and PNG only");
    }

    private static String readApplicationYaml() throws IOException {
        try (InputStream stream = FileStorageConfigurationTest.class
                .getClassLoader()
                .getResourceAsStream("application.yml")) {
            if (stream == null) {
                throw new IOException("application.yml is not available on the test classpath");
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n");
        }
    }
}
