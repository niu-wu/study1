package com.example.study11.service.impl;

import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.dao.RetestApplicationDao;
import com.example.study11.dao.RetestReviewDao;
import com.example.study11.entity.dto.RecruitmentStatusTransitionDTO;
import com.example.study11.entity.enums.RecruitmentStatus;
import com.example.study11.entity.enums.RecruitmentType;
import com.example.study11.entity.enums.StatusTransitionAction;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.RetestApplicationPo;
import com.example.study11.entity.po.RetestReviewPo;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.entity.vo.RetestDetailsVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.RecruitmentStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetestServiceImplTest {

    @Mock
    private RecruitmentInfoDao recruitmentInfoDao;

    @Mock
    private RetestApplicationDao retestApplicationDao;

    @Mock
    private RetestReviewDao retestReviewDao;

    @Mock
    private RecruitmentStatusService recruitmentStatusService;

    @InjectMocks
    private RetestServiceImpl retestService;

    @Test
    void applyInsertsApplicationAndSubmitsRetestReview() {
        RecruitmentInfoPo current = recruitment("record-1", RecruitmentStatus.PENDING_INITIAL,
                RecruitmentType.INTERNAL);
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1")).thenReturn(current);
        when(retestApplicationDao.selectByRecordUuid("record-1")).thenReturn(null);
        when(retestApplicationDao.insert(any(RetestApplicationPo.class))).thenAnswer(invocation -> {
            RetestApplicationPo application = invocation.getArgument(0);
            application.setId(9L);
            return 1;
        });
        RecruitmentInfoVO transitioned = recruitmentVo("record-1", RecruitmentStatus.RETEST_REVIEW,
                RecruitmentType.INTERNAL);
        when(recruitmentStatusService.transition(eq("record-1"), any(RecruitmentStatusTransitionDTO.class),
                eq(15))).thenReturn(transitioned);
        RetestApplicationPo saved = application(9L, "record-1", 15);
        when(retestApplicationDao.selectByRecordUuid("record-1")).thenReturn(null, saved);

        RetestDetailsVO result = retestService.apply("record-1", "初试通过", 15);

        ArgumentCaptor<RetestApplicationPo> applicationCaptor =
                ArgumentCaptor.forClass(RetestApplicationPo.class);
        verify(retestApplicationDao).insert(applicationCaptor.capture());
        assertEquals("record-1", applicationCaptor.getValue().getRecordUuid());
        assertEquals("初试通过", applicationCaptor.getValue().getApplicantRemark());
        assertEquals(15, applicationCaptor.getValue().getApplicantUserId());
        ArgumentCaptor<RecruitmentStatusTransitionDTO> transitionCaptor =
                ArgumentCaptor.forClass(RecruitmentStatusTransitionDTO.class);
        verify(recruitmentStatusService).transition(eq("record-1"), transitionCaptor.capture(), eq(15));
        assertEquals(StatusTransitionAction.SUBMIT_RETEST_REVIEW, transitionCaptor.getValue().getAction());
        assertEquals(RecruitmentStatus.RETEST_REVIEW.getCode(), result.getStatus());
        assertNotNull(result.getApplication());
    }

    @Test
    void applyRejectsDuplicateApplicationBeforeInsert() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_INITIAL, RecruitmentType.INTERNAL));
        when(retestApplicationDao.selectByRecordUuid("record-1"))
                .thenReturn(application(8L, "record-1", 15));

        ApiException exception = assertThrows(ApiException.class,
                () -> retestService.apply("record-1", null, 15));

        assertEquals(409, exception.getStatus().value());
        verify(retestApplicationDao, never()).insert(any());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void applyReturnsConflictForDuplicateAfterStatusAlreadyMovedToReview() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.RETEST_REVIEW, RecruitmentType.INTERNAL));
        when(retestApplicationDao.selectByRecordUuid("record-1"))
                .thenReturn(application(8L, "record-1", 15));

        ApiException exception = assertThrows(ApiException.class,
                () -> retestService.apply("record-1", "重复申请", 15));

        assertEquals(409, exception.getStatus().value());
        verify(retestApplicationDao, never()).insert(any());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void applyRejectsTerminalOrNonInitialStatus() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.REJECTED, RecruitmentType.INTERNAL));

        ApiException exception = assertThrows(ApiException.class,
                () -> retestService.apply("record-1", null, 15));

        assertEquals(422, exception.getStatus().value());
        verify(retestApplicationDao).selectByRecordUuid("record-1");
        verify(retestApplicationDao, never()).insert(any());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void confirmRequiresExistingApplicationAndRetestReviewStatus() {
        RecruitmentInfoPo current = recruitment("record-1", RecruitmentStatus.RETEST_REVIEW,
                RecruitmentType.INTERNAL);
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1")).thenReturn(current);
        when(retestApplicationDao.selectByRecordUuid("record-1")).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> retestService.confirm("record-1", null, null, null, 26));

        assertEquals(409, exception.getStatus().value());
        verify(retestReviewDao, never()).insert(any());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void confirmRequiresOutsourcedFields() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.RETEST_REVIEW, RecruitmentType.OUTSOURCED));
        when(retestApplicationDao.selectByRecordUuid("record-1"))
                .thenReturn(application(8L, "record-1", 15));
        when(retestReviewDao.selectByRecordUuid("record-1")).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> retestService.confirm("record-1", "外派公司", "", null, 26));

        assertEquals(400, exception.getStatus().value());
        verify(retestReviewDao, never()).insert(any());
    }

    @Test
    void confirmInsertsReviewAndConfirmsRetest() {
        LocalDateTime retestTime = LocalDateTime.of(2026, 8, 30, 10, 0);
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.RETEST_REVIEW, RecruitmentType.INTERNAL));
        when(retestApplicationDao.selectByRecordUuid("record-1"))
                .thenReturn(application(8L, "record-1", 15));
        when(retestReviewDao.selectByRecordUuid("record-1")).thenReturn(null);
        when(retestReviewDao.insert(any(RetestReviewPo.class))).thenAnswer(invocation -> {
            RetestReviewPo review = invocation.getArgument(0);
            review.setId(12L);
            return 1;
        });
        when(recruitmentStatusService.transition(eq("record-1"), any(RecruitmentStatusTransitionDTO.class),
                eq(26))).thenReturn(recruitmentVo("record-1", RecruitmentStatus.PENDING_RETEST,
                RecruitmentType.INTERNAL));
        RetestReviewPo saved = review(12L, "record-1", 26);
        saved.setRetestTime(retestTime);
        when(retestReviewDao.selectByRecordUuid("record-1")).thenReturn(null, saved);

        RetestDetailsVO result = retestService.confirm("record-1", null, null, retestTime, 26);

        ArgumentCaptor<RetestReviewPo> reviewCaptor = ArgumentCaptor.forClass(RetestReviewPo.class);
        verify(retestReviewDao).insert(reviewCaptor.capture());
        assertEquals("record-1", reviewCaptor.getValue().getRecordUuid());
        assertEquals(retestTime, reviewCaptor.getValue().getRetestTime());
        assertEquals(26, reviewCaptor.getValue().getReviewerUserId());
        ArgumentCaptor<RecruitmentStatusTransitionDTO> transitionCaptor =
                ArgumentCaptor.forClass(RecruitmentStatusTransitionDTO.class);
        verify(recruitmentStatusService).transition(eq("record-1"), transitionCaptor.capture(), eq(26));
        assertEquals(StatusTransitionAction.CONFIRM_RETEST, transitionCaptor.getValue().getAction());
        assertEquals(RecruitmentStatus.PENDING_RETEST.getCode(), result.getStatus());
        assertNotNull(result.getReview());
    }

    @Test
    void confirmRejectsDuplicateReview() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.RETEST_REVIEW, RecruitmentType.INTERNAL));
        when(retestApplicationDao.selectByRecordUuid("record-1"))
                .thenReturn(application(8L, "record-1", 15));
        when(retestReviewDao.selectByRecordUuid("record-1"))
                .thenReturn(review(12L, "record-1", 26));

        ApiException exception = assertThrows(ApiException.class,
                () -> retestService.confirm("record-1", null, null, null, 26));

        assertEquals(409, exception.getStatus().value());
        verify(retestReviewDao, never()).insert(any());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void findByRecordUuidReturnsStatusTypeAndNestedRecords() {
        when(recruitmentInfoDao.selectByRecordUuid("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.RETEST_REVIEW, RecruitmentType.OUTSOURCED));
        when(retestApplicationDao.selectByRecordUuid("record-1"))
                .thenReturn(application(8L, "record-1", 15));
        when(retestReviewDao.selectByRecordUuid("record-1"))
                .thenReturn(review(12L, "record-1", 26));

        RetestDetailsVO result = retestService.findByRecordUuid("record-1");

        assertEquals("record-1", result.getRecordUuid());
        assertEquals(RecruitmentStatus.RETEST_REVIEW.getCode(), result.getStatus());
        assertEquals(RecruitmentType.OUTSOURCED, result.getRecruitmentType());
        assertEquals(8L, result.getApplication().getId());
        assertEquals(12L, result.getReview().getId());
    }

    @Test
    void findByRecordUuidReturnsNotFoundForMissingRecruitment() {
        when(recruitmentInfoDao.selectByRecordUuid("missing-record")).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> retestService.findByRecordUuid("missing-record"));

        assertEquals(404, exception.getStatus().value());
        verifyNoInteractions(retestApplicationDao, retestReviewDao);
    }

    private static RecruitmentInfoPo recruitment(String recordUuid, RecruitmentStatus status,
                                                  RecruitmentType type) {
        RecruitmentInfoPo result = new RecruitmentInfoPo();
        result.setRecordUuid(recordUuid);
        result.setStatus(status.getCode());
        result.setRecruitmentType(type);
        return result;
    }

    private static RecruitmentInfoVO recruitmentVo(String recordUuid, RecruitmentStatus status,
                                                    RecruitmentType type) {
        RecruitmentInfoVO result = new RecruitmentInfoVO();
        result.setRecordUuid(recordUuid);
        result.setStatus(status.getCode());
        result.setRecruitmentType(type);
        return result;
    }

    private static RetestApplicationPo application(Long id, String recordUuid, Integer userId) {
        RetestApplicationPo result = new RetestApplicationPo();
        result.setId(id);
        result.setRecordUuid(recordUuid);
        result.setApplicantUserId(userId);
        result.setApplicationTime(LocalDateTime.now());
        return result;
    }

    private static RetestReviewPo review(Long id, String recordUuid, Integer userId) {
        RetestReviewPo result = new RetestReviewPo();
        result.setId(id);
        result.setRecordUuid(recordUuid);
        result.setReviewerUserId(userId);
        result.setReviewTime(LocalDateTime.now());
        return result;
    }
}
