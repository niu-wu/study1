package com.example.study11.utils;

import com.example.study11.config.PhotoStorageProperties;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhotoFileUtilsTest {

    @TempDir
    Path temporaryDirectory;

    private PhotoFileUtils photoFileUtils;

    @BeforeEach
    void setUp() {
        photoFileUtils = new PhotoFileUtils(validProperties());
    }

    @Test
    void generatesDistinctUuidNamesWithNormalizedAllowedExtension() {
        String first = photoFileUtils.generateStoredFilename("Photo.JPG");
        String second = photoFileUtils.generateStoredFilename("Photo.JPG");

        assertTrue(first.endsWith(".jpg"));
        assertTrue(second.endsWith(".jpg"));
        assertNotEquals(first, second);
        assertDoesNotThrow(() -> UUID.fromString(first.substring(0, first.length() - 4)));
    }

    @Test
    void rejectsDisallowedAndAmbiguousFilenames() {
        assertBadRequest(() -> photoFileUtils.generateStoredFilename("photo.gif"));
        assertBadRequest(() -> photoFileUtils.generateStoredFilename("photo.jpg.exe"));
        assertBadRequest(() -> photoFileUtils.generateStoredFilename("../photo.jpg"));
        assertBadRequest(() -> photoFileUtils.generateStoredFilename("nested/photo.png"));
    }

    @Test
    void resolvesCanonicalImageContentType() {
        assertEquals("image/jpeg", photoFileUtils.resolveCanonicalContentType("photo.JPEG"));
        assertEquals("image/png", photoFileUtils.resolveCanonicalContentType("photo.png"));
        assertBadRequest(() -> photoFileUtils.resolveCanonicalContentType("photo.gif"));
    }

    @Test
    void rejectsTraversalWhenResolvingStoredPath() {
        assertBadRequest(() -> photoFileUtils.resolveStoragePath("../outside.jpg"));
        assertBadRequest(() -> photoFileUtils.resolveStoragePath("nested/photo.png"));
    }

    private PhotoStorageProperties validProperties() {
        PhotoStorageProperties properties = new PhotoStorageProperties();
        properties.setUploadDir(temporaryDirectory.toString());
        properties.setMaxFileSize(DataSize.ofBytes(10));
        properties.setAllowedExtensions(List.of("jpg", "jpeg", "png"));
        return properties;
    }

    private static void assertBadRequest(Executable executable) {
        ApiException exception = assertThrows(ApiException.class, executable);
        assertEquals(400, exception.getStatus().value());
    }
}
