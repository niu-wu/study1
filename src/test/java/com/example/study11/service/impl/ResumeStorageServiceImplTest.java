package com.example.study11.service.impl;

import com.example.study11.config.FileStorageProperties;
import com.example.study11.dao.CandidateResumeDao;
import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.entity.po.CandidateResumePo;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.vo.CandidateResumeVO;
import com.example.study11.exception.ApiException;
import com.example.study11.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeStorageServiceImplTest {

    private static final String RECORD_UUID = "550e8400-e29b-41d4-a716-446655440000";
    private static final Integer UPLOADER_USER_ID = 7;

    @TempDir
    Path temporaryDirectory;

    @Mock
    private CandidateResumeDao candidateResumeDao;

    @Mock
    private RecruitmentInfoDao recruitmentInfoDao;

    private FileUtils fileUtils;

    private ResumeStorageServiceImpl resumeStorageService;

    @BeforeEach
    void setUp() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setUploadDir(temporaryDirectory.toString());
        properties.setMaxFileSize(DataSize.ofBytes(10));
        properties.setAllowedExtensions(List.of("pdf", "doc", "docx"));
        fileUtils = new FileUtils(properties);
        resumeStorageService = new ResumeStorageServiceImpl(candidateResumeDao, recruitmentInfoDao, fileUtils);
    }

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void storesBytesWithRandomizedFilenameAndDatabaseUploadedAt() throws Exception {
        byte[] content = "resume".getBytes();
        CandidateResumePo inserted = new CandidateResumePo();
        LocalDateTime uploadedAt = LocalDateTime.of(2026, 8, 24, 12, 30);
        when(recruitmentInfoDao.selectByRecordUuidForUpdate(RECORD_UUID))
                .thenReturn(existingRecruitment());
        when(candidateResumeDao.insert(any(CandidateResumePo.class))).thenAnswer(invocation -> {
            CandidateResumePo po = invocation.getArgument(0);
            po.setId(42L);
            inserted.setId(42L);
            inserted.setRecordUuid(po.getRecordUuid());
            inserted.setOriginalFilename(po.getOriginalFilename());
            inserted.setStoredFilename(po.getStoredFilename());
            inserted.setFileSize(po.getFileSize());
            inserted.setContentType(po.getContentType());
            inserted.setUploaderUserId(po.getUploaderUserId());
            return 1;
        });
        when(candidateResumeDao.selectById(42L)).thenAnswer(invocation -> {
            inserted.setUploadedAt(uploadedAt);
            return inserted;
        });

        CandidateResumeVO result = resumeStorageService.storeFile(RECORD_UUID,
                new MockMultipartFile("file", "resume.PDF", "text/plain", content), UPLOADER_USER_ID);

        assertEquals(42L, result.getId());
        assertEquals(RECORD_UUID, result.getRecordUuid());
        assertEquals("resume.PDF", result.getOriginalFilename());
        assertEquals((long) content.length, result.getFileSize());
        assertEquals("application/pdf", result.getContentType());
        assertEquals(UPLOADER_USER_ID, result.getUploaderUserId());
        assertEquals(uploadedAt, result.getUploadedAt());
        assertNotNull(inserted.getStoredFilename());
        assertTrue(inserted.getStoredFilename().matches(
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.pdf"));
        assertArrayEquals(content, Files.readAllBytes(fileUtils.resolveStoragePath(inserted.getStoredFilename())));
        verify(candidateResumeDao).insert(any(CandidateResumePo.class));
    }

    @Test
    void rejectsInvalidExtensionWithoutWritingOrInserting() {
        ApiException exception = assertThrows(ApiException.class,
                () -> resumeStorageService.storeFile(RECORD_UUID,
                        new MockMultipartFile("file", "resume.txt", "application/pdf", new byte[]{1}),
                        UPLOADER_USER_ID));

        assertEquals(400, exception.getStatus().value());
        verifyNoInteractions(candidateResumeDao, recruitmentInfoDao);
        assertDirectoryEmpty();
    }

    @Test
    void rejectsEmptyAndOversizedFilesWithoutWritingOrInserting() {
        ApiException empty = assertThrows(ApiException.class,
                () -> resumeStorageService.storeFile(RECORD_UUID,
                        new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]),
                        UPLOADER_USER_ID));
        ApiException oversized = assertThrows(ApiException.class,
                () -> resumeStorageService.storeFile(RECORD_UUID,
                        new MockMultipartFile("file", "large.pdf", "application/pdf", new byte[11]),
                        UPLOADER_USER_ID));

        assertEquals(400, empty.getStatus().value());
        assertEquals(400, oversized.getStatus().value());
        verifyNoInteractions(candidateResumeDao, recruitmentInfoDao);
        assertDirectoryEmpty();
    }

    @Test
    void rejectsMissingRecordAfterTemporaryWriteAndLeavesNoArtifact() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate(RECORD_UUID)).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> resumeStorageService.storeFile(RECORD_UUID,
                        multipart("resume.pdf", new byte[]{1, 2}), UPLOADER_USER_ID));

        assertEquals(404, exception.getStatus().value());
        verify(candidateResumeDao, never()).insert(any(CandidateResumePo.class));
        assertDirectoryEmpty();
    }

    @Test
    void convertsInputIoFailureWithoutInsertingOrLeavingArtifact() throws Exception {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("resume.pdf");
        when(file.getSize()).thenReturn(2L);
        when(file.getInputStream()).thenThrow(new IOException("input failure"));

        ApiException exception = assertThrows(ApiException.class,
                () -> resumeStorageService.storeFile(RECORD_UUID, file, UPLOADER_USER_ID));

        assertEquals(500, exception.getStatus().value());
        verifyNoInteractions(candidateResumeDao, recruitmentInfoDao);
        assertDirectoryEmpty();
    }

    @Test
    void cleansFinalArtifactWhenInsertReturnsZero() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate(RECORD_UUID)).thenReturn(existingRecruitment());
        when(candidateResumeDao.insert(any(CandidateResumePo.class))).thenReturn(0);

        ApiException exception = assertThrows(ApiException.class,
                () -> resumeStorageService.storeFile(RECORD_UUID,
                        multipart("resume.pdf", new byte[]{1, 2}), UPLOADER_USER_ID));

        assertEquals(500, exception.getStatus().value());
        assertDirectoryEmpty();
    }

    @Test
    void cleansFinalArtifactWhenInsertThrows() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate(RECORD_UUID)).thenReturn(existingRecruitment());
        when(candidateResumeDao.insert(any(CandidateResumePo.class)))
                .thenThrow(new IllegalStateException("database failure"));

        ApiException exception = assertThrows(ApiException.class,
                () -> resumeStorageService.storeFile(RECORD_UUID,
                        multipart("resume.pdf", new byte[]{1, 2}), UPLOADER_USER_ID));

        assertEquals(500, exception.getStatus().value());
        assertFalse(exception.getMessage().contains("database failure"));
        assertDirectoryEmpty();
    }

    @Test
    void cleansFinalArtifactWhenRequeryFails() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate(RECORD_UUID)).thenReturn(existingRecruitment());
        when(candidateResumeDao.insert(any(CandidateResumePo.class))).thenAnswer(invocation -> {
            CandidateResumePo po = invocation.getArgument(0);
            po.setId(43L);
            return 1;
        });
        when(candidateResumeDao.selectById(43L)).thenThrow(new IllegalStateException("requery failure"));

        ApiException exception = assertThrows(ApiException.class,
                () -> resumeStorageService.storeFile(RECORD_UUID,
                        multipart("resume.pdf", new byte[]{1, 2}), UPLOADER_USER_ID));

        assertEquals(500, exception.getStatus().value());
        assertDirectoryEmpty();
    }

    @Test
    void validatesRequiredStoreArguments() {
        ApiException blankRecord = assertThrows(ApiException.class,
                () -> resumeStorageService.storeFile(" ", multipart("resume.pdf", new byte[]{1}), UPLOADER_USER_ID));
        ApiException nullFile = assertThrows(ApiException.class,
                () -> resumeStorageService.storeFile(RECORD_UUID, null, UPLOADER_USER_ID));
        ApiException unauthenticated = assertThrows(ApiException.class,
                () -> resumeStorageService.storeFile(RECORD_UUID, multipart("resume.pdf", new byte[]{1}), null));

        assertEquals(400, blankRecord.getStatus().value());
        assertEquals(400, nullFile.getStatus().value());
        assertEquals(401, unauthenticated.getStatus().value());
        verifyNoInteractions(candidateResumeDao, recruitmentInfoDao);
    }

    @Test
    void loadsOnlyExistingRegularControlledFile() throws Exception {
        String storedFilename = fileUtils.generateStoredFilename("resume.pdf");
        Path path = fileUtils.resolveStoragePath(storedFilename);
        Files.createDirectories(path.getParent());
        byte[] content = new byte[]{4, 5, 6};
        Files.write(path, content);
        CandidateResumePo metadata = metadata(9L, storedFilename);
        when(candidateResumeDao.selectById(9L)).thenReturn(metadata);

        Resource result = resumeStorageService.loadFile(9L);

        assertArrayEquals(content, result.getInputStream().readAllBytes());
        assertEquals(path.toAbsolutePath(), result.getFile().toPath().toAbsolutePath());
        verify(candidateResumeDao).selectById(9L);
        verify(candidateResumeDao, never()).selectByIdForUpdate(9L);
    }

    @Test
    void findsMetadataByIdWithoutExposingStoredFilename() {
        CandidateResumePo source = metadata(9L, "550e8400-e29b-41d4-a716-446655440000.pdf");
        source.setOriginalFilename("resume.pdf");
        source.setFileSize(3L);
        source.setContentType("application/pdf");
        source.setUploaderUserId(7);
        when(candidateResumeDao.selectById(9L)).thenReturn(source);

        CandidateResumeVO result = resumeStorageService.findById(9L);

        assertEquals(9L, result.getId());
        assertEquals("resume.pdf", result.getOriginalFilename());
        assertEquals("application/pdf", result.getContentType());
        verify(candidateResumeDao).selectById(9L);
    }

    @Test
    void findsMetadataListOnlyWhenParentRecordExists() {
        when(recruitmentInfoDao.selectByRecordUuid(RECORD_UUID)).thenReturn(existingRecruitment());
        CandidateResumePo source = metadata(9L, "550e8400-e29b-41d4-a716-446655440000.pdf");
        source.setOriginalFilename("resume.pdf");
        when(candidateResumeDao.selectByRecordUuid(RECORD_UUID)).thenReturn(List.of(source));

        List<CandidateResumeVO> result = resumeStorageService.findByRecordUuid(RECORD_UUID);

        assertEquals(1, result.size());
        assertEquals(9L, result.get(0).getId());
        assertEquals("resume.pdf", result.get(0).getOriginalFilename());
        verify(recruitmentInfoDao).selectByRecordUuid(RECORD_UUID);
        verify(candidateResumeDao).selectByRecordUuid(RECORD_UUID);
    }

    @Test
    void rejectsMetadataListForUnknownRecord() {
        when(recruitmentInfoDao.selectByRecordUuid(RECORD_UUID)).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> resumeStorageService.findByRecordUuid(RECORD_UUID));

        assertEquals(404, exception.getStatus().value());
        verify(candidateResumeDao, never()).selectByRecordUuid(RECORD_UUID);
    }

    @Test
    void loadReturnsNotFoundForMissingMetadataOrPhysicalFile() {
        when(candidateResumeDao.selectById(10L)).thenReturn(null);
        ApiException metadataMissing = assertThrows(ApiException.class,
                () -> resumeStorageService.loadFile(10L));

        String storedFilename = fileUtils.generateStoredFilename("resume.pdf");
        when(candidateResumeDao.selectById(11L)).thenReturn(metadata(11L, storedFilename));
        ApiException physicalMissing = assertThrows(ApiException.class,
                () -> resumeStorageService.loadFile(11L));

        assertEquals(404, metadataMissing.getStatus().value());
        assertEquals(404, physicalMissing.getStatus().value());
    }

    @Test
    void loadReturnsNotFoundForCorruptedStoredFilename() {
        when(candidateResumeDao.selectById(12L)).thenReturn(metadata(12L, "resume.exe"));

        ApiException exception = assertThrows(ApiException.class,
                () -> resumeStorageService.loadFile(12L));

        assertEquals(404, exception.getStatus().value());
    }

    @Test
    void deletesMetadataBeforeControlledPhysicalFile() throws Exception {
        String storedFilename = fileUtils.generateStoredFilename("resume.pdf");
        Path path = fileUtils.resolveStoragePath(storedFilename);
        Files.createDirectories(path.getParent());
        Files.write(path, new byte[]{1});
        when(candidateResumeDao.selectByIdForUpdate(12L)).thenReturn(metadata(12L, storedFilename));
        when(candidateResumeDao.deleteById(12L)).thenReturn(1);

        resumeStorageService.deleteFile(12L);

        assertFalse(Files.exists(path));
        org.mockito.InOrder order = org.mockito.Mockito.inOrder(candidateResumeDao);
        order.verify(candidateResumeDao).selectByIdForUpdate(12L);
        order.verify(candidateResumeDao).deleteById(12L);
    }

    @Test
    void deleteReturnsNotFoundWhenMetadataIsMissing() {
        when(candidateResumeDao.selectByIdForUpdate(15L)).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> resumeStorageService.deleteFile(15L));

        assertEquals(404, exception.getStatus().value());
        verify(candidateResumeDao, never()).deleteById(15L);
    }

    @Test
    void deleteLeavesPhysicalFileWhenMetadataDeleteFails() throws Exception {
        String storedFilename = fileUtils.generateStoredFilename("resume.pdf");
        Path path = fileUtils.resolveStoragePath(storedFilename);
        Files.createDirectories(path.getParent());
        Files.write(path, new byte[]{1});
        when(candidateResumeDao.selectByIdForUpdate(13L)).thenReturn(metadata(13L, storedFilename));
        when(candidateResumeDao.deleteById(13L)).thenReturn(0);

        ApiException exception = assertThrows(ApiException.class,
                () -> resumeStorageService.deleteFile(13L));

        assertEquals(500, exception.getStatus().value());
        assertTrue(Files.exists(path));
    }

    @Test
    void rollsBackWhenPhysicalDeleteFails() throws Exception {
        String storedFilename = fileUtils.generateStoredFilename("resume.pdf");
        Path path = fileUtils.resolveStoragePath(storedFilename);
        Files.createDirectories(path);
        Files.write(path.resolve("child"), new byte[]{1});
        when(candidateResumeDao.selectByIdForUpdate(16L)).thenReturn(metadata(16L, storedFilename));
        when(candidateResumeDao.deleteById(16L)).thenReturn(1);

        ApiException exception = assertThrows(ApiException.class,
                () -> resumeStorageService.deleteFile(16L));

        assertEquals(500, exception.getStatus().value());
        assertTrue(Files.exists(path));
    }

    @Test
    void restoresPhysicalFileWhenOuterTransactionRollsBack() throws Exception {
        String storedFilename = fileUtils.generateStoredFilename("resume.pdf");
        Path path = fileUtils.resolveStoragePath(storedFilename);
        Files.createDirectories(path.getParent());
        Files.write(path, new byte[]{7, 8, 9});
        when(candidateResumeDao.selectByIdForUpdate(17L)).thenReturn(metadata(17L, storedFilename));
        when(candidateResumeDao.deleteById(17L)).thenReturn(1);
        TransactionSynchronizationManager.initSynchronization();

        resumeStorageService.deleteFile(17L);

        assertFalse(Files.exists(path));
        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());
        synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        assertTrue(Files.exists(path));
        assertArrayEquals(new byte[]{7, 8, 9}, Files.readAllBytes(path));
    }

    @Test
    void removesQuarantineAfterOuterTransactionCommits() throws Exception {
        String storedFilename = fileUtils.generateStoredFilename("resume.pdf");
        Path path = fileUtils.resolveStoragePath(storedFilename);
        Files.createDirectories(path.getParent());
        Files.write(path, new byte[]{10, 11});
        when(candidateResumeDao.selectByIdForUpdate(18L)).thenReturn(metadata(18L, storedFilename));
        when(candidateResumeDao.deleteById(18L)).thenReturn(1);
        TransactionSynchronizationManager.initSynchronization();

        resumeStorageService.deleteFile(18L);

        assertFalse(Files.exists(path));
        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());
        synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);

        try (var entries = Files.list(temporaryDirectory)) {
            assertEquals(0L, entries.count());
        }
    }

    @Test
    void rollbackSynchronizationRemovesFinalArtifact() throws Exception {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate(RECORD_UUID)).thenReturn(existingRecruitment());
        when(candidateResumeDao.insert(any(CandidateResumePo.class))).thenAnswer(invocation -> {
            CandidateResumePo po = invocation.getArgument(0);
            po.setId(14L);
            return 1;
        });
        when(candidateResumeDao.selectById(14L)).thenAnswer(invocation -> metadataFromInsert());
        TransactionSynchronizationManager.initSynchronization();

        CandidateResumeVO result = resumeStorageService.storeFile(RECORD_UUID,
                multipart("resume.pdf", new byte[]{1, 2}), UPLOADER_USER_ID);
        CandidateResumePo inserted = latestInsertedMetadata();
        Path path = fileUtils.resolveStoragePath(inserted.getStoredFilename());
        assertTrue(Files.exists(path));

        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());
        synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        assertNotNull(result);
        assertFalse(Files.exists(path));
    }

    @Test
    void allowsTwoRandomFilesForSameRecord() throws Exception {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate(RECORD_UUID)).thenReturn(existingRecruitment());
        AtomicLong nextId = new AtomicLong(20L);
        List<CandidateResumePo> inserted = new ArrayList<>();
        when(candidateResumeDao.insert(any(CandidateResumePo.class))).thenAnswer(invocation -> {
            CandidateResumePo po = invocation.getArgument(0);
            po.setId(nextId.getAndIncrement());
            inserted.add(po);
            return 1;
        });
        when(candidateResumeDao.selectById(any(Long.class))).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            CandidateResumePo saved = inserted.stream().filter(item -> item.getId().equals(id)).findFirst().orElseThrow();
            saved.setUploadedAt(LocalDateTime.now());
            return saved;
        });

        resumeStorageService.storeFile(RECORD_UUID, multipart("resume.pdf", new byte[]{1}), UPLOADER_USER_ID);
        resumeStorageService.storeFile(RECORD_UUID, multipart("resume.pdf", new byte[]{2}), UPLOADER_USER_ID);

        assertEquals(2, inserted.size());
        assertFalse(inserted.get(0).getStoredFilename().equals(inserted.get(1).getStoredFilename()));
        assertTrue(Files.exists(fileUtils.resolveStoragePath(inserted.get(0).getStoredFilename())));
        assertTrue(Files.exists(fileUtils.resolveStoragePath(inserted.get(1).getStoredFilename())));
    }

    private CandidateResumePo latestInsertedMetadata() {
        ArgumentCaptor<CandidateResumePo> captor = ArgumentCaptor.forClass(CandidateResumePo.class);
        verify(candidateResumeDao).insert(captor.capture());
        return captor.getValue();
    }

    private CandidateResumePo metadataFromInsert() {
        ArgumentCaptor<CandidateResumePo> captor = ArgumentCaptor.forClass(CandidateResumePo.class);
        verify(candidateResumeDao).insert(captor.capture());
        CandidateResumePo inserted = captor.getValue();
        inserted.setId(14L);
        inserted.setUploadedAt(LocalDateTime.of(2026, 8, 24, 12, 0));
        return inserted;
    }

    private MultipartFile multipart(String filename, byte[] content) {
        return new MockMultipartFile("file", filename, "application/octet-stream", content);
    }

    private RecruitmentInfoPo existingRecruitment() {
        RecruitmentInfoPo po = new RecruitmentInfoPo();
        po.setRecordUuid(RECORD_UUID);
        return po;
    }

    private CandidateResumePo metadata(Long id, String storedFilename) {
        CandidateResumePo po = new CandidateResumePo();
        po.setId(id);
        po.setRecordUuid(RECORD_UUID);
        po.setStoredFilename(storedFilename);
        return po;
    }

    private void assertDirectoryEmpty() {
        try (var entries = Files.list(temporaryDirectory)) {
            assertEquals(0L, entries.count());
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
