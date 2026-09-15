package com.example.study11.service.impl;

import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.EmployeePhotoStorageService;
import com.example.study11.utils.PhotoFileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/** 一寸照本地存储。只保存 UUID 文件名，不把绝对路径写入数据库。 */
@Service
public class EmployeePhotoStorageServiceImpl implements EmployeePhotoStorageService {

    private static final String STORAGE_FAILURE_MESSAGE = "一寸照存储失败";

    private static final String PHOTO_NOT_FOUND_MESSAGE = "一寸照不存在";

    private final PhotoFileUtils photoFileUtils;

    public EmployeePhotoStorageServiceImpl(PhotoFileUtils photoFileUtils) {
        this.photoFileUtils = photoFileUtils;
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        photoFileUtils.validateOriginalFilename(originalFilename);
        photoFileUtils.validateFileSize(file.getSize());
        String storedFilename = photoFileUtils.generateStoredFilename(originalFilename);
        Path finalPath = photoFileUtils.resolveStoragePath(storedFilename);
        Path temporaryPath = null;
        boolean finalMoved = false;
        try {
            temporaryPath = createTemporaryFile(finalPath);
            copyUpload(file, temporaryPath);
            photoFileUtils.validateFileSize(Files.size(temporaryPath));
            moveWithoutReplacing(temporaryPath, finalPath);
            finalMoved = true;
            return storedFilename;
        } catch (ApiException exception) {
            cleanup(temporaryPath);
            if (finalMoved) {
                cleanup(finalPath);
            }
            throw exception;
        } catch (IOException | RuntimeException exception) {
            cleanup(temporaryPath);
            if (finalMoved) {
                cleanup(finalPath);
            }
            throw ApiException.internalServerError(STORAGE_FAILURE_MESSAGE);
        } finally {
            cleanup(temporaryPath);
        }
    }

    @Override
    public EmployeePhotoFileVO load(String storedFilename) {
        if (storedFilename == null || storedFilename.isBlank()) {
            throw ApiException.notFound(PHOTO_NOT_FOUND_MESSAGE);
        }
        Path path;
        try {
            path = photoFileUtils.resolveStoragePath(storedFilename);
        } catch (ApiException exception) {
            throw ApiException.notFound(PHOTO_NOT_FOUND_MESSAGE);
        }
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw ApiException.notFound(PHOTO_NOT_FOUND_MESSAGE);
        }
        EmployeePhotoFileVO result = new EmployeePhotoFileVO();
        result.setResource(new FileSystemResource(path));
        result.setContentType(photoFileUtils.resolveCanonicalContentType(storedFilename));
        result.setFilename("photo." + extensionOf(storedFilename));
        return result;
    }

    @Override
    public void deleteQuietly(String storedFilename) {
        if (storedFilename == null || storedFilename.isBlank()) {
            return;
        }
        try {
            Path path = photoFileUtils.resolveStoragePath(storedFilename);
            Files.deleteIfExists(path);
        } catch (ApiException | IOException | SecurityException ignored) {
            // 旧照片清理失败不能挡住新照片保存。
        }
    }

    private static Path createTemporaryFile(Path finalPath) throws IOException {
        Path parent = finalPath.getParent();
        Files.createDirectories(parent);
        return Files.createTempFile(parent, ".photo-", ".tmp");
    }

    private static void copyUpload(MultipartFile file, Path temporaryPath) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(temporaryPath, StandardOpenOption.WRITE,
                     StandardOpenOption.TRUNCATE_EXISTING)) {
            inputStream.transferTo(outputStream);
        }
    }

    private static void moveWithoutReplacing(Path temporaryPath, Path finalPath) throws IOException {
        try {
            Files.move(temporaryPath, finalPath, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryPath, finalPath);
        }
    }

    private static String extensionOf(String storedFilename) {
        int extensionIndex = storedFilename.lastIndexOf('.');
        return storedFilename.substring(extensionIndex + 1);
    }

    private static void cleanup(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException | SecurityException ignored) {
            // 文件补偿失败不能覆盖原始业务异常。
        }
    }
}
