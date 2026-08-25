package com.example.study11.service.impl;

import com.example.study11.dao.CandidateResumeDao;
import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.entity.po.CandidateResumePo;
import com.example.study11.entity.vo.CandidateResumeVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.ResumeStorageService;
import com.example.study11.utils.FileUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
import java.util.List;
import java.util.stream.Collectors;

/** 候选人简历附件存储服务实现。 */
@Service
public class ResumeStorageServiceImpl implements ResumeStorageService {

    private static final String INVALID_RECORD_UUID_MESSAGE = "招聘记录 UUID 不能为空";
    private static final String FILE_NOT_FOUND_MESSAGE = "简历附件不存在";
    private static final String STORAGE_FAILURE_MESSAGE = "简历附件存储失败";
    private static final String DELETE_FAILURE_MESSAGE = "简历附件删除失败";

    private final CandidateResumeDao candidateResumeDao;

    private final RecruitmentInfoDao recruitmentInfoDao;

    private final FileUtils fileUtils;

    public ResumeStorageServiceImpl(CandidateResumeDao candidateResumeDao,
                                    RecruitmentInfoDao recruitmentInfoDao,
                                    FileUtils fileUtils) {
        this.candidateResumeDao = candidateResumeDao;
        this.recruitmentInfoDao = recruitmentInfoDao;
        this.fileUtils = fileUtils;
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateResumeVO findById(Long id) {
        validateId(id);
        CandidateResumePo metadata = candidateResumeDao.selectById(id);
        if (metadata == null) {
            throw ApiException.notFound(FILE_NOT_FOUND_MESSAGE);
        }
        return toVo(metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateResumeVO> findByRecordUuid(String recordUuid) {
        if (recordUuid == null || recordUuid.isBlank()) {
            throw ApiException.badRequest(INVALID_RECORD_UUID_MESSAGE);
        }
        if (recruitmentInfoDao.selectByRecordUuid(recordUuid) == null) {
            throw ApiException.notFound("招聘信息不存在");
        }
        return candidateResumeDao.selectByRecordUuid(recordUuid).stream()
                .map(ResumeStorageServiceImpl::toVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CandidateResumeVO storeFile(String recordUuid, MultipartFile file, Integer uploaderUserId) {
        validateStoreArguments(recordUuid, file, uploaderUserId);

        String originalFilename = file.getOriginalFilename();
        fileUtils.validateOriginalFilename(originalFilename);
        fileUtils.validateFileSize(file.getSize());
        String storedFilename = fileUtils.generateStoredFilename(originalFilename);
        Path finalPath = fileUtils.resolveStoragePath(storedFilename);
        Path temporaryPath = null;
        boolean finalMoved = false;
        try {
            temporaryPath = createTemporaryFile(finalPath);
            copyUpload(file, temporaryPath);
            long actualFileSize = Files.size(temporaryPath);
            fileUtils.validateFileSize(actualFileSize);

            if (recruitmentInfoDao.selectByRecordUuidForUpdate(recordUuid) == null) {
                throw ApiException.notFound("招聘信息不存在");
            }

            moveWithoutReplacing(temporaryPath, finalPath);
            finalMoved = true;
            registerRollbackCleanup(finalPath);

            CandidateResumePo candidateResumePo = new CandidateResumePo();
            candidateResumePo.setRecordUuid(recordUuid);
            candidateResumePo.setOriginalFilename(originalFilename);
            candidateResumePo.setStoredFilename(storedFilename);
            candidateResumePo.setFileSize(actualFileSize);
            candidateResumePo.setContentType(fileUtils.resolveCanonicalContentType(originalFilename));
            candidateResumePo.setUploaderUserId(uploaderUserId);
            if (candidateResumeDao.insert(candidateResumePo) != 1 || candidateResumePo.getId() == null) {
                throw ApiException.internalServerError(STORAGE_FAILURE_MESSAGE);
            }

            CandidateResumePo saved = candidateResumeDao.selectById(candidateResumePo.getId());
            if (saved == null) {
                throw ApiException.internalServerError(STORAGE_FAILURE_MESSAGE);
            }
            return toVo(saved);
        } catch (ApiException exception) {
            cleanup(temporaryPath);
            if (finalMoved) {
                cleanup(finalPath);
            }
            throw exception;
        } catch (IOException exception) {
            cleanup(temporaryPath);
            if (finalMoved) {
                cleanup(finalPath);
            }
            throw ApiException.internalServerError(STORAGE_FAILURE_MESSAGE);
        } catch (RuntimeException exception) {
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
    @Transactional(readOnly = true)
    public Resource loadFile(Long id) {
        validateId(id);
        CandidateResumePo metadata = candidateResumeDao.selectById(id);
        if (metadata == null) {
            throw ApiException.notFound(FILE_NOT_FOUND_MESSAGE);
        }
        Path path;
        try {
            path = fileUtils.resolveStoragePath(metadata.getStoredFilename());
        } catch (ApiException exception) {
            throw ApiException.notFound(FILE_NOT_FOUND_MESSAGE);
        }
        if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw ApiException.notFound(FILE_NOT_FOUND_MESSAGE);
        }
        return new FileSystemResource(path);
    }

    @Override
    @Transactional
    public void deleteFile(Long id) {
        validateId(id);
        CandidateResumePo metadata = candidateResumeDao.selectByIdForUpdate(id);
        if (metadata == null) {
            throw ApiException.notFound(FILE_NOT_FOUND_MESSAGE);
        }
        Path path = fileUtils.resolveStoragePath(metadata.getStoredFilename());
        Path quarantinePath = null;
        try {
            quarantinePath = moveToQuarantine(path);
            int deleted = candidateResumeDao.deleteById(id);
            if (deleted != 1) {
                restoreQuarantine(path, quarantinePath);
                throw ApiException.internalServerError(DELETE_FAILURE_MESSAGE);
            }
            if (quarantinePath == null) {
                return;
            }
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                registerDeleteSynchronization(path, quarantinePath);
            } else {
                finalizeDirectDelete(path, quarantinePath);
            }
        } catch (ApiException exception) {
            restoreQuarantine(path, quarantinePath);
            throw exception;
        } catch (IOException | SecurityException exception) {
            restoreQuarantine(path, quarantinePath);
            throw ApiException.internalServerError(DELETE_FAILURE_MESSAGE);
        } catch (RuntimeException exception) {
            restoreQuarantine(path, quarantinePath);
            throw ApiException.internalServerError(DELETE_FAILURE_MESSAGE);
        }
    }

    private static void validateStoreArguments(String recordUuid, MultipartFile file, Integer uploaderUserId) {
        if (recordUuid == null || recordUuid.isBlank()) {
            throw ApiException.badRequest(INVALID_RECORD_UUID_MESSAGE);
        }
        if (file == null) {
            throw ApiException.badRequest("上传文件不能为空");
        }
        if (uploaderUserId == null || uploaderUserId <= 0) {
            throw ApiException.unauthorized("用户未登录");
        }
    }

    private static void validateId(Long id) {
        if (id == null || id <= 0) {
            throw ApiException.badRequest("简历附件编号必须为正数");
        }
    }

    private static Path createTemporaryFile(Path finalPath) throws IOException {
        Path parent = finalPath.getParent();
        Files.createDirectories(parent);
        return Files.createTempFile(parent, ".resume-", ".tmp");
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

    private static Path moveToQuarantine(Path originalPath) throws IOException {
        if (!Files.exists(originalPath, LinkOption.NOFOLLOW_LINKS)) {
            return null;
        }
        Path parent = originalPath.getParent();
        Path quarantinePath = Files.createTempFile(parent, ".resume-delete-", ".tmp");
        try {
            Files.deleteIfExists(quarantinePath);
            moveWithoutReplacing(originalPath, quarantinePath);
            return quarantinePath;
        } catch (IOException | SecurityException exception) {
            cleanup(quarantinePath);
            throw exception;
        }
    }

    private static void finalizeDirectDelete(Path originalPath, Path quarantinePath) throws IOException {
        try {
            Files.deleteIfExists(quarantinePath);
        } catch (IOException | SecurityException exception) {
            restoreQuarantine(originalPath, quarantinePath);
            throw exception;
        }
    }

    private static void registerDeleteSynchronization(Path originalPath, Path quarantinePath) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_COMMITTED) {
                    cleanup(quarantinePath);
                } else if (status == TransactionSynchronization.STATUS_ROLLED_BACK
                        || status == TransactionSynchronization.STATUS_UNKNOWN) {
                    restoreQuarantine(originalPath, quarantinePath);
                }
            }
        });
    }

    private static void restoreQuarantine(Path originalPath, Path quarantinePath) {
        if (quarantinePath == null || !Files.exists(quarantinePath, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        try {
            moveWithoutReplacing(quarantinePath, originalPath);
        } catch (IOException | SecurityException ignored) {
            // 补偿失败不能覆盖原始业务异常。
        }
    }

    private static void registerRollbackCleanup(Path finalPath) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK
                        || status == TransactionSynchronization.STATUS_UNKNOWN) {
                    cleanup(finalPath);
                }
            }
        });
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

    private static CandidateResumeVO toVo(CandidateResumePo source) {
        CandidateResumeVO result = new CandidateResumeVO();
        result.setId(source.getId());
        result.setRecordUuid(source.getRecordUuid());
        result.setOriginalFilename(source.getOriginalFilename());
        result.setFileSize(source.getFileSize());
        result.setContentType(source.getContentType());
        result.setUploaderUserId(source.getUploaderUserId());
        result.setUploadedAt(source.getUploadedAt());
        return result;
    }
}
