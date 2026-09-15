package com.example.study11.service.impl;

import com.example.study11.config.PhotoStorageProperties;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.exception.ApiException;
import com.example.study11.utils.PhotoFileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeePhotoStorageServiceImplTest {

    @TempDir
    Path temporaryDirectory;

    private PhotoFileUtils photoFileUtils;

    private EmployeePhotoStorageServiceImpl service;

    @BeforeEach
    void setUp() {
        PhotoStorageProperties properties = new PhotoStorageProperties();
        properties.setUploadDir(temporaryDirectory.toString());
        properties.setMaxFileSize(DataSize.ofBytes(10));
        properties.setAllowedExtensions(List.of("jpg", "jpeg", "png"));
        photoFileUtils = new PhotoFileUtils(properties);
        service = new EmployeePhotoStorageServiceImpl(photoFileUtils);
    }

    @Test
    void storePersistsUuidFilenameWithoutReplacingExistingFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.PNG", "image/png", new byte[]{1, 2, 3});

        String storedFilename = service.store(file);

        assertTrue(storedFilename.endsWith(".png"));
        Path storedPath = photoFileUtils.resolveStoragePath(storedFilename);
        assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(storedPath));
        assertFalse(storedFilename.contains("/") || storedFilename.contains("\\"));
    }

    @Test
    void storeRejectsDisallowedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.gif", "image/gif", new byte[]{1});

        ApiException exception = assertThrows(ApiException.class, () -> service.store(file));

        assertEquals(400, exception.getStatus().value());
    }

    @Test
    void loadReturnsResourceForExistingPhoto() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{9, 8, 7});
        String storedFilename = service.store(file);

        EmployeePhotoFileVO result = service.load(storedFilename);

        assertEquals("image/jpeg", result.getContentType());
        assertEquals("photo.jpg", result.getFilename());
        assertTrue(result.getResource().exists());
        assertArrayEquals(new byte[]{9, 8, 7}, result.getResource().getInputStream().readAllBytes());
    }

    @Test
    void loadMissingFileReturnsNotFound() {
        ApiException exception = assertThrows(ApiException.class,
                () -> service.load("550e8400-e29b-41d4-a716-446655440000.jpg"));

        assertEquals(404, exception.getStatus().value());
    }

    @Test
    void deleteQuietlyIgnoresMissingFile() {
        service.deleteQuietly("550e8400-e29b-41d4-a716-446655440000.jpg");
        service.deleteQuietly(" ");
    }
}
