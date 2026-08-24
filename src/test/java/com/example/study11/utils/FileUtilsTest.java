package com.example.study11.utils;

import com.example.study11.config.FileStorageProperties;
import com.example.study11.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.function.Executable;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilsTest {

    @TempDir
    Path temporaryDirectory;

    private FileUtils fileUtils;

    @BeforeEach
    void setUp() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setUploadDir(temporaryDirectory.toString());
        properties.setMaxFileSize(DataSize.ofBytes(10));
        properties.setAllowedExtensions(List.of("pdf", "doc", "docx"));
        fileUtils = new FileUtils(properties);
    }

    @Test
    void generatesDistinctUuidNamesWithNormalizedAllowedExtension() {
        String first = fileUtils.generateStoredFilename("Resume.PDF");
        String second = fileUtils.generateStoredFilename("Resume.PDF");

        assertTrue(first.endsWith(".pdf"));
        assertTrue(second.endsWith(".pdf"));
        assertNotEquals(first, second);
        assertDoesNotThrow(() -> UUID.fromString(first.substring(0, first.length() - 4)));
        assertDoesNotThrow(() -> UUID.fromString(second.substring(0, second.length() - 4)));
    }

    @Test
    void onlyAllowsConfiguredExtensionsAndRejectsAmbiguousNames() {
        assertTrue(fileUtils.isAllowedExtension("resume.PDF"));
        assertTrue(fileUtils.isAllowedExtension("resume.doc"));
        assertTrue(fileUtils.isAllowedExtension("resume.docx"));
        assertFalse(fileUtils.isAllowedExtension(null));
        assertFalse(fileUtils.isAllowedExtension("resume"));
        assertFalse(fileUtils.isAllowedExtension(".pdf"));
        assertFalse(fileUtils.isAllowedExtension("resume.pdf.exe"));
        assertFalse(fileUtils.isAllowedExtension("resume.png"));

        assertBadRequest(() -> fileUtils.generateStoredFilename("resume.pdf.exe"));
    }

    @Test
    void rejectsEmptyAndOversizedFiles() {
        assertDoesNotThrow(() -> fileUtils.validateFileSize(10));

        assertBadRequest(() -> fileUtils.validateFileSize(0));
        assertBadRequest(() -> fileUtils.validateFileSize(-1));
        assertBadRequest(() -> fileUtils.validateFileSize(11));
    }

    @Test
    void rejectsOriginalFilenameLongerThanDatabaseColumn() {
        String maximumLengthFilename = "a".repeat(251) + ".pdf";
        String oversizedFilename = "a".repeat(252) + ".pdf";

        assertDoesNotThrow(() -> fileUtils.validateOriginalFilename(maximumLengthFilename));
        assertBadRequest(() -> fileUtils.validateOriginalFilename(oversizedFilename));
    }

    @Test
    void resolvesCanonicalContentTypeFromAllowedExtension() {
        assertEquals("application/pdf", fileUtils.resolveCanonicalContentType("resume.PDF"));
        assertEquals("application/msword", fileUtils.resolveCanonicalContentType("resume.doc"));
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                fileUtils.resolveCanonicalContentType("resume.docx"));
        assertBadRequest(() -> fileUtils.resolveCanonicalContentType("resume.txt"));
    }

    @Test
    void resolvesOnlyGeneratedNamesBelowTheNormalizedStorageRoot() {
        String storedFilename = "550e8400-e29b-41d4-a716-446655440000.pdf";

        Path result = fileUtils.resolveStoragePath(storedFilename);

        assertEquals(temporaryDirectory.toAbsolutePath().normalize().resolve(storedFilename), result);
    }

    @Test
    void rejectsTraversalAbsoluteAndNestedPaths() {
        assertBadRequest(() -> fileUtils.resolveStoragePath("../outside.pdf"));
        assertBadRequest(() -> fileUtils.resolveStoragePath("..\\outside.pdf"));
        assertBadRequest(() -> fileUtils.resolveStoragePath("nested/resume.pdf"));
        assertBadRequest(() -> fileUtils.resolveStoragePath("C:\\outside.pdf"));
        assertBadRequest(() -> fileUtils.resolveStoragePath("/outside.pdf"));
    }

    @Test
    void rejectsBlankStorageRootConfiguration() {
        FileStorageProperties properties = validProperties();
        properties.setUploadDir(" ");

        assertInvalidConfiguration(() -> new FileUtils(properties));
    }

    @Test
    void rejectsNonPositiveMaximumFileSizeConfiguration() {
        FileStorageProperties properties = validProperties();
        properties.setMaxFileSize(DataSize.ofBytes(0));

        assertInvalidConfiguration(() -> new FileUtils(properties));
    }

    @Test
    void rejectsInvalidAllowedExtensionConfiguration() {
        FileStorageProperties properties = validProperties();
        properties.setAllowedExtensions(List.of("pdf", " "));

        assertInvalidConfiguration(() -> new FileUtils(properties));
    }

    private FileStorageProperties validProperties() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setUploadDir(temporaryDirectory.toString());
        properties.setMaxFileSize(DataSize.ofBytes(10));
        properties.setAllowedExtensions(List.of("pdf", "doc", "docx"));
        return properties;
    }

    private static void assertBadRequest(Executable executable) {
        ApiException exception = assertThrows(ApiException.class, executable);
        assertEquals(400, exception.getStatus().value());
    }

    private static void assertInvalidConfiguration(Executable executable) {
        assertThrows(IllegalStateException.class, executable);
    }
}
