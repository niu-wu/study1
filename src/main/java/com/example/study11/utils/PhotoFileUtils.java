package com.example.study11.utils;

import com.example.study11.config.PhotoStorageProperties;
import com.example.study11.exception.ApiException;
import org.springframework.stereotype.Component;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** 一寸照文件名、大小和存储路径校验。 */
@Component
public class PhotoFileUtils {

    private static final int MAX_ORIGINAL_FILENAME_LENGTH = 255;

    private static final String INVALID_CONFIGURATION_MESSAGE = "照片存储配置无效";
    private static final String INVALID_FILENAME_MESSAGE = "照片文件名或扩展名不合法";
    private static final String INVALID_FILE_SIZE_MESSAGE = "照片大小不合法";
    private static final String INVALID_STORED_FILENAME_MESSAGE = "照片存储文件名不合法";

    private final Path storageRoot;
    private final long maxFileSizeBytes;
    private final Set<String> allowedExtensions;

    public PhotoFileUtils(PhotoStorageProperties photoStorageProperties) {
        if (photoStorageProperties == null) {
            throw invalidConfiguration();
        }
        this.storageRoot = resolveStorageRoot(photoStorageProperties.getUploadDir());
        this.maxFileSizeBytes = resolveMaxFileSize(photoStorageProperties.getMaxFileSize());
        this.allowedExtensions = resolveAllowedExtensions(photoStorageProperties.getAllowedExtensions());
    }

    public String generateStoredFilename(String originalFilename) {
        validateOriginalFilename(originalFilename);
        String extension = extractAllowedExtension(originalFilename);
        return UUID.randomUUID() + "." + extension;
    }

    public void validateOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.length() > MAX_ORIGINAL_FILENAME_LENGTH
                || extractAllowedExtension(originalFilename) == null) {
            throw ApiException.badRequest(INVALID_FILENAME_MESSAGE);
        }
    }

    public String resolveCanonicalContentType(String originalFilename) {
        validateOriginalFilename(originalFilename);
        String extension = extractAllowedExtension(originalFilename);
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> throw ApiException.badRequest(INVALID_FILENAME_MESSAGE);
        };
    }

    public void validateFileSize(long fileSize) {
        if (fileSize <= 0 || fileSize > maxFileSizeBytes) {
            throw ApiException.badRequest(INVALID_FILE_SIZE_MESSAGE);
        }
    }

    public Path resolveStoragePath(String storedFilename) {
        if (!isGeneratedStoredFilename(storedFilename)) {
            throw ApiException.badRequest(INVALID_STORED_FILENAME_MESSAGE);
        }
        Path storagePath = storageRoot.resolve(storedFilename).normalize();
        if (!storagePath.startsWith(storageRoot)) {
            throw ApiException.badRequest(INVALID_STORED_FILENAME_MESSAGE);
        }
        return storagePath;
    }

    private Path resolveStorageRoot(String uploadDir) {
        if (uploadDir == null || uploadDir.isBlank()) {
            throw invalidConfiguration();
        }
        try {
            return Path.of(uploadDir).toAbsolutePath().normalize();
        } catch (InvalidPathException exception) {
            throw invalidConfiguration();
        }
    }

    private long resolveMaxFileSize(org.springframework.util.unit.DataSize maxFileSize) {
        if (maxFileSize == null || maxFileSize.toBytes() <= 0) {
            throw invalidConfiguration();
        }
        return maxFileSize.toBytes();
    }

    private Set<String> resolveAllowedExtensions(List<String> configuredExtensions) {
        if (configuredExtensions == null || configuredExtensions.isEmpty()) {
            throw invalidConfiguration();
        }
        Set<String> normalizedExtensions = new LinkedHashSet<>();
        for (String configuredExtension : configuredExtensions) {
            if (configuredExtension == null) {
                throw invalidConfiguration();
            }
            String normalizedExtension = configuredExtension.trim().toLowerCase(Locale.ROOT);
            if (!isExtensionToken(normalizedExtension)) {
                throw invalidConfiguration();
            }
            normalizedExtensions.add(normalizedExtension);
        }
        if (normalizedExtensions.isEmpty()) {
            throw invalidConfiguration();
        }
        return Collections.unmodifiableSet(normalizedExtensions);
    }

    private String extractAllowedExtension(String filename) {
        if (!isSimpleClientFilename(filename)) {
            return null;
        }
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex <= 0 || extensionIndex == filename.length() - 1) {
            return null;
        }
        String extension = filename.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
        if (!isExtensionToken(extension) || !allowedExtensions.contains(extension)) {
            return null;
        }
        return extension;
    }

    private boolean isGeneratedStoredFilename(String storedFilename) {
        if (!isSimpleClientFilename(storedFilename)) {
            return false;
        }
        int extensionIndex = storedFilename.lastIndexOf('.');
        if (extensionIndex <= 0 || extensionIndex == storedFilename.length() - 1) {
            return false;
        }
        String uuidText = storedFilename.substring(0, extensionIndex);
        String extension = storedFilename.substring(extensionIndex + 1);
        if (!extension.equals(extension.toLowerCase(Locale.ROOT))
                || !isExtensionToken(extension)
                || !allowedExtensions.contains(extension)) {
            return false;
        }
        try {
            return UUID.fromString(uuidText).toString().equals(uuidText);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean isSimpleClientFilename(String filename) {
        if (filename == null
                || filename.isBlank()
                || !filename.equals(filename.strip())
                || filename.charAt(0) == '.'
                || filename.indexOf('/') >= 0
                || filename.indexOf('\\') >= 0
                || filename.indexOf(':') >= 0
                || filename.indexOf('\0') >= 0) {
            return false;
        }
        for (int index = 0; index < filename.length(); index++) {
            if (Character.isISOControl(filename.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    private boolean isExtensionToken(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        for (int index = 0; index < extension.length(); index++) {
            char character = extension.charAt(index);
            if (!((character >= 'a' && character <= 'z') || (character >= '0' && character <= '9'))) {
                return false;
            }
        }
        return true;
    }

    private IllegalStateException invalidConfiguration() {
        return new IllegalStateException(INVALID_CONFIGURATION_MESSAGE);
    }
}
