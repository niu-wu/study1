package com.example.study11.utils;

import com.example.study11.config.FileStorageProperties;
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

/**
 * 文件存储校验工具。
 */
@Component
public class FileUtils {

    private static final int MAX_ORIGINAL_FILENAME_LENGTH = 255;

    private static final String INVALID_CONFIGURATION_MESSAGE = "文件存储配置无效";
    private static final String INVALID_FILENAME_MESSAGE = "文件名或扩展名不合法";
    private static final String INVALID_FILE_SIZE_MESSAGE = "文件大小不合法";
    private static final String INVALID_STORED_FILENAME_MESSAGE = "存储文件名不合法";

    private final Path storageRoot;
    private final long maxFileSizeBytes;
    private final Set<String> allowedExtensions;

    public FileUtils(FileStorageProperties fileStorageProperties) {
        if (fileStorageProperties == null) {
            throw invalidConfiguration();
        }
        this.storageRoot = resolveStorageRoot(fileStorageProperties.getUploadDir());
        this.maxFileSizeBytes = resolveMaxFileSize(fileStorageProperties.getMaxFileSize());
        this.allowedExtensions = resolveAllowedExtensions(fileStorageProperties.getAllowedExtensions());
    }

    /**
     * 根据原始文件名生成安全的存储文件名。
     *
     * @param originalFilename 客户端提交的原始文件名
     * @return UUID 格式的存储文件名
     */
    public String generateStoredFilename(String originalFilename) {
        validateOriginalFilename(originalFilename);
        String extension = extractAllowedExtension(originalFilename);
        return UUID.randomUUID() + "." + extension;
    }

    /**
     * 校验原始文件名，长度与数据库列保持一致。
     *
     * @param originalFilename 客户端提交的原始文件名
     */
    public void validateOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.length() > MAX_ORIGINAL_FILENAME_LENGTH
                || extractAllowedExtension(originalFilename) == null) {
            throw ApiException.badRequest(INVALID_FILENAME_MESSAGE);
        }
    }

    /**
     * 按受控扩展名解析规范 MIME，完全忽略客户端声明的 MIME。
     *
     * @param originalFilename 客户端提交的原始文件名
     * @return 规范 MIME
     */
    public String resolveCanonicalContentType(String originalFilename) {
        validateOriginalFilename(originalFilename);
        String extension = extractAllowedExtension(originalFilename);
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> throw ApiException.badRequest(INVALID_FILENAME_MESSAGE);
        };
    }

    /**
     * 判断文件名扩展名是否在允许范围内。
     *
     * @param filename 待校验文件名
     * @return 允许返回 {@code true}，否则返回 {@code false}
     */
    public boolean isAllowedExtension(String filename) {
        return extractAllowedExtension(filename) != null;
    }

    /**
     * 校验上传文件大小。
     *
     * @param fileSize 文件大小，单位为字节
     */
    public void validateFileSize(long fileSize) {
        if (fileSize <= 0 || fileSize > maxFileSizeBytes) {
            throw ApiException.badRequest(INVALID_FILE_SIZE_MESSAGE);
        }
    }

    /**
     * 解析受限于上传根目录的存储路径。
     *
     * @param storedFilename 已生成的存储文件名
     * @return 规范化后的绝对存储路径
     */
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
