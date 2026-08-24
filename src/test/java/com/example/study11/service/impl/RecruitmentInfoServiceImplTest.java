package com.example.study11.service.impl;

import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.dao.CandidateResumeDao;
import com.example.study11.dao.RecruitmentStatusHistoryDao;
import com.example.study11.entity.dto.RecruitmentInfoCreateDTO;
import com.example.study11.entity.dto.RecruitmentInfoUpdateDTO;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruitmentInfoServiceImplTest {

    @Mock
    private RecruitmentInfoDao recruitmentInfoDao;

    @Mock
    private RecruitmentStatusHistoryDao recruitmentStatusHistoryDao;

    @Mock
    private CandidateResumeDao candidateResumeDao;

    @InjectMocks
    private RecruitmentInfoServiceImpl recruitmentInfoService;

    @Test
    void createGeneratesRecordUuidAndDefaultStatusWithoutAcceptingDatabaseFields() {
        RecruitmentInfoCreateDTO request = new RecruitmentInfoCreateDTO();
        request.setApplicantName("张三");
        request.setPosition("Java开发工程师");

        when(recruitmentInfoDao.insert(any(RecruitmentInfoPo.class))).thenAnswer(invocation -> {
            RecruitmentInfoPo inserted = invocation.getArgument(0);
            inserted.setId(11L);
            return 1;
        });
        when(recruitmentInfoDao.selectByRecordUuid(any(String.class)))
                .thenAnswer(invocation -> {
                    RecruitmentInfoPo saved = new RecruitmentInfoPo();
                    saved.setRecordUuid(invocation.getArgument(0));
                    saved.setId(11L);
                    saved.setApplicantName("张三");
                    saved.setPosition("Java开发工程师");
                    saved.setStatus("PENDING_INITIAL");
                    return saved;
                });

        RecruitmentInfoVO result = recruitmentInfoService.create(request);

        ArgumentCaptor<RecruitmentInfoPo> captor = ArgumentCaptor.forClass(RecruitmentInfoPo.class);
        verify(recruitmentInfoDao).insert(captor.capture());
        RecruitmentInfoPo inserted = captor.getValue();
        assertNotNull(inserted.getRecordUuid());
        assertEquals("PENDING_INITIAL", inserted.getStatus());
        assertEquals(11L, result.getId());
        assertEquals(inserted.getRecordUuid(), result.getRecordUuid());
    }

    @Test
    void updatePreservesCurrentStatusBecauseStatusIsNotAnUpdateField() {
        RecruitmentInfoPo current = new RecruitmentInfoPo();
        current.setRecordUuid("record-1");
        current.setStatus("PENDING_RETEST");
        current.setApplicantName("旧姓名");
        current.setPosition("旧岗位");
        when(recruitmentInfoDao.selectByRecordUuid("record-1")).thenReturn(current);
        when(recruitmentInfoDao.updateByRecordUuid(any(RecruitmentInfoPo.class))).thenReturn(1);
        when(recruitmentInfoDao.selectByRecordUuid("record-1")).thenAnswer(invocation -> current);

        RecruitmentInfoUpdateDTO request = new RecruitmentInfoUpdateDTO();
        request.setApplicantName("新姓名");
        request.setPosition("新岗位");
        request.setInitialInterviewTime(LocalDateTime.of(2026, 8, 23, 10, 0));

        RecruitmentInfoVO result = recruitmentInfoService.update("record-1", request);

        ArgumentCaptor<RecruitmentInfoPo> captor = ArgumentCaptor.forClass(RecruitmentInfoPo.class);
        verify(recruitmentInfoDao).updateByRecordUuid(captor.capture());
        assertEquals("record-1", captor.getValue().getRecordUuid());
        assertEquals("PENDING_RETEST", captor.getValue().getStatus());
        assertEquals("PENDING_RETEST", result.getStatus());
    }

    @Test
    void createRejectsMissingApplicantNameOrPosition() {
        RecruitmentInfoCreateDTO request = new RecruitmentInfoCreateDTO();
        request.setPosition("Java开发工程师");

        ApiException exception = assertThrows(ApiException.class,
                () -> recruitmentInfoService.create(request));

        assertEquals(400, exception.getStatus().value());
    }

    @Test
    void deleteDeletesLockedRecordWithoutHistoryInLockHistoryDeleteOrder() {
        String recordUuid = "record-without-history";
        RecruitmentInfoPo existing = new RecruitmentInfoPo();
        existing.setRecordUuid(recordUuid);
        when(recruitmentInfoDao.selectByRecordUuidForUpdate(recordUuid)).thenReturn(existing);
        when(recruitmentStatusHistoryDao.countByRecordUuid(recordUuid)).thenReturn(0L);
        when(candidateResumeDao.countByRecordUuid(recordUuid)).thenReturn(0L);
        when(recruitmentInfoDao.deleteByRecordUuid(recordUuid)).thenReturn(1);

        assertDoesNotThrow(() -> recruitmentInfoService.delete(recordUuid));

        InOrder inOrder = inOrder(recruitmentInfoDao, recruitmentStatusHistoryDao, candidateResumeDao);
        inOrder.verify(recruitmentInfoDao).selectByRecordUuidForUpdate(recordUuid);
        inOrder.verify(recruitmentStatusHistoryDao).countByRecordUuid(recordUuid);
        inOrder.verify(candidateResumeDao).countByRecordUuid(recordUuid);
        inOrder.verify(recruitmentInfoDao).deleteByRecordUuid(recordUuid);
    }

    @Test
    void deleteRejectsRecordWithResumeWithoutDeletingParent() {
        RecruitmentInfoPo existing = new RecruitmentInfoPo();
        existing.setRecordUuid("record-with-resume");
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-with-resume"))
                .thenReturn(existing);
        when(recruitmentStatusHistoryDao.countByRecordUuid("record-with-resume"))
                .thenReturn(0L);
        when(candidateResumeDao.countByRecordUuid("record-with-resume"))
                .thenReturn(1L);

        ApiException exception = assertThrows(ApiException.class,
                () -> recruitmentInfoService.delete("record-with-resume"));

        assertEquals(409, exception.getStatus().value());
        assertEquals("招聘记录已有简历附件，不能删除", exception.getMessage());
        verify(recruitmentInfoDao, never()).deleteByRecordUuid("record-with-resume");
    }

    @Test
    void deleteRejectsRecordWithStatusHistoryWithoutDeletingParent() {
        RecruitmentInfoPo existing = new RecruitmentInfoPo();
        existing.setRecordUuid("record-with-history");
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-with-history"))
                .thenReturn(existing);
        when(recruitmentStatusHistoryDao.countByRecordUuid("record-with-history"))
                .thenReturn(1L);

        ApiException exception = assertThrows(ApiException.class,
                () -> recruitmentInfoService.delete("record-with-history"));

        assertEquals(409, exception.getStatus().value());
        assertEquals("招聘记录已有状态历史，不能删除", exception.getMessage());
        verify(recruitmentInfoDao, never()).deleteByRecordUuid("record-with-history");
    }

    @Test
    void deleteRejectsBlankRecordUuidBeforeCallingDaos() {
        ApiException exception = assertThrows(ApiException.class,
                () -> recruitmentInfoService.delete(" "));

        assertEquals(400, exception.getStatus().value());
        verifyNoInteractions(recruitmentInfoDao, recruitmentStatusHistoryDao);
    }

    @Test
    void deleteReturnsNotFoundWhenLockedRecordDoesNotExist() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("missing-record"))
                .thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> recruitmentInfoService.delete("missing-record"));

        assertEquals(404, exception.getStatus().value());
        verify(recruitmentInfoDao).selectByRecordUuidForUpdate("missing-record");
        verify(recruitmentStatusHistoryDao, never()).countByRecordUuid("missing-record");
        verify(recruitmentInfoDao, never()).deleteByRecordUuid("missing-record");
    }

    @Test
    void deleteReturnsInternalServerErrorWhenLockedRecordIsNotDeleted() {
        RecruitmentInfoPo existing = new RecruitmentInfoPo();
        existing.setRecordUuid("record-not-deleted");
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-not-deleted"))
                .thenReturn(existing);
        when(recruitmentStatusHistoryDao.countByRecordUuid("record-not-deleted"))
                .thenReturn(0L);
        when(candidateResumeDao.countByRecordUuid("record-not-deleted"))
                .thenReturn(0L);
        when(recruitmentInfoDao.deleteByRecordUuid("record-not-deleted")).thenReturn(0);

        ApiException exception = assertThrows(ApiException.class,
                () -> recruitmentInfoService.delete("record-not-deleted"));

        assertEquals(500, exception.getStatus().value());
        verify(recruitmentInfoDao).deleteByRecordUuid("record-not-deleted");
    }
}
