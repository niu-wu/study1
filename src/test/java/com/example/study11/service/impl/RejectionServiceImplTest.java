package com.example.study11.service.impl;

import com.example.study11.dao.CandidateResumeDao;
import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.dao.RecruitmentRejectionDao;
import com.example.study11.entity.dto.RecruitmentStatusTransitionDTO;
import com.example.study11.entity.enums.RecruitmentStatus;
import com.example.study11.entity.enums.RejectionReason;
import com.example.study11.entity.enums.RejectionStage;
import com.example.study11.entity.enums.StatusTransitionAction;
import com.example.study11.entity.enums.TalentCategory;
import com.example.study11.entity.po.CandidateResumePo;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.RecruitmentRejectionPo;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.entity.vo.RecruitmentRejectionVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.RecruitmentStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RejectionServiceImplTest {

    @Mock
    private RecruitmentInfoDao recruitmentInfoDao;

    @Mock
    private RecruitmentRejectionDao recruitmentRejectionDao;

    @Mock
    private CandidateResumeDao candidateResumeDao;

    @Mock
    private RecruitmentStatusService recruitmentStatusService;

    @InjectMocks
    private RejectionServiceImpl rejectionService;

    @Test
    void rejectStoresReasonStageCategoryAndTransitionsAnyActiveStatus() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.RETEST_REVIEW));
        when(recruitmentRejectionDao.selectByRecordUuid("record-1")).thenReturn(null);
        when(recruitmentRejectionDao.insert(any(RecruitmentRejectionPo.class))).thenAnswer(invocation -> {
            RecruitmentRejectionPo rejection = invocation.getArgument(0);
            rejection.setId(11L);
            return 1;
        });
        RecruitmentInfoVO transitioned = recruitmentVo("record-1", RecruitmentStatus.REJECTED);
        when(recruitmentStatusService.transition(eq("record-1"), any(RecruitmentStatusTransitionDTO.class),
                eq(15))).thenReturn(transitioned);

        RecruitmentRejectionVO result = rejectionService.reject("record-1", RejectionStage.RETEST,
                RejectionReason.SKILL_MISMATCH, TalentCategory.FUTURE_CONSIDER,
                "技能不匹配", null, 15);

        ArgumentCaptor<RecruitmentRejectionPo> captor =
                ArgumentCaptor.forClass(RecruitmentRejectionPo.class);
        verify(recruitmentRejectionDao).insert(captor.capture());
        RecruitmentRejectionPo saved = captor.getValue();
        assertEquals("record-1", saved.getRecordUuid());
        assertEquals(RejectionStage.RETEST.getCode(), saved.getRejectionStage());
        assertEquals(RejectionReason.SKILL_MISMATCH.getCode(), saved.getRejectionReason());
        assertEquals(TalentCategory.FUTURE_CONSIDER.getCode(), saved.getTalentCategory());
        assertEquals(15, saved.getOperatorUserId());
        assertEquals("REJECT", saved.getAction());
        ArgumentCaptor<RecruitmentStatusTransitionDTO> transitionCaptor =
                ArgumentCaptor.forClass(RecruitmentStatusTransitionDTO.class);
        verify(recruitmentStatusService).transition(eq("record-1"), transitionCaptor.capture(), eq(15));
        assertEquals(StatusTransitionAction.REJECT, transitionCaptor.getValue().getAction());
        assertEquals(RecruitmentStatus.REJECTED.getCode(), result.getStatus());
    }

    @Test
    void rejectRequiresTalentCategory() {
        ApiException exception = assertThrows(ApiException.class,
                () -> rejectionService.reject("record-1", RejectionStage.INITIAL_INTERVIEW,
                        RejectionReason.OTHER, null, null, null, 15));

        assertEquals(400, exception.getStatus().value());
        verifyNoInteractions(recruitmentRejectionDao, recruitmentStatusService);
    }

    @Test
    void rejectDoesNotOperateOnTerminalStatus() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.ONBOARDED));

        ApiException exception = assertThrows(ApiException.class,
                () -> rejectionService.reject("record-1", RejectionStage.ONBOARDING,
                        RejectionReason.OTHER, TalentCategory.NOT_SUITABLE, null, null, 15));

        assertEquals(422, exception.getStatus().value());
        verify(recruitmentRejectionDao, never()).insert(any());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void rejectValidatesAttachmentBelongsToRecruitmentRecord() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_INITIAL));
        when(recruitmentRejectionDao.selectByRecordUuid("record-1")).thenReturn(null);
        CandidateResumePo resume = new CandidateResumePo();
        resume.setId(99L);
        resume.setRecordUuid("other-record");
        when(candidateResumeDao.selectById(99L)).thenReturn(resume);

        ApiException exception = assertThrows(ApiException.class,
                () -> rejectionService.reject("record-1", RejectionStage.INITIAL_INTERVIEW,
                        RejectionReason.OTHER, TalentCategory.NOT_SUITABLE, null, 99L, 15));

        assertEquals(400, exception.getStatus().value());
        verify(recruitmentRejectionDao, never()).insert(any());
    }

    @Test
    void declineUsesFixedStageAndReasonAndTransitionsOnlyPendingOnboarding() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_ONBOARDING));
        when(recruitmentRejectionDao.selectByRecordUuid("record-1")).thenReturn(null);
        when(recruitmentRejectionDao.insert(any(RecruitmentRejectionPo.class))).thenReturn(1);
        when(recruitmentStatusService.transition(eq("record-1"), any(RecruitmentStatusTransitionDTO.class),
                eq(15))).thenReturn(recruitmentVo("record-1", RecruitmentStatus.DECLINED));

        RecruitmentRejectionVO result = rejectionService.decline("record-1",
                TalentCategory.SKILL_BACKUP, "候选人主动放弃", 15);

        ArgumentCaptor<RecruitmentRejectionPo> captor =
                ArgumentCaptor.forClass(RecruitmentRejectionPo.class);
        verify(recruitmentRejectionDao).insert(captor.capture());
        assertEquals("DECLINE", captor.getValue().getAction());
        assertEquals(RejectionStage.ONBOARDING.getCode(), captor.getValue().getRejectionStage());
        assertEquals(RejectionReason.CANDIDATE_DECLINED.getCode(), captor.getValue().getRejectionReason());
        assertEquals(RecruitmentStatus.DECLINED.getCode(), result.getStatus());
    }

    @Test
    void declineRejectsNonOnboardingStatus() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_RETEST));

        ApiException exception = assertThrows(ApiException.class,
                () -> rejectionService.decline("record-1", TalentCategory.NOT_SUITABLE, null, 15));

        assertEquals(422, exception.getStatus().value());
        verify(recruitmentRejectionDao, never()).insert(any());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void duplicateRejectionReturnsConflict() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_INITIAL));
        when(recruitmentRejectionDao.selectByRecordUuid("record-1"))
                .thenReturn(rejection(3L, "record-1", "REJECT"));

        ApiException exception = assertThrows(ApiException.class,
                () -> rejectionService.reject("record-1", RejectionStage.INITIAL_INTERVIEW,
                        RejectionReason.OTHER, TalentCategory.NOT_SUITABLE, null, null, 15));

        assertEquals(409, exception.getStatus().value());
        verify(recruitmentRejectionDao, never()).insert(any());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void duplicateRejectionInTerminalStateReturnsConflict() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.REJECTED));
        when(recruitmentRejectionDao.selectByRecordUuid("record-1"))
                .thenReturn(rejection(3L, "record-1", "REJECT"));

        ApiException exception = assertThrows(ApiException.class,
                () -> rejectionService.reject("record-1", RejectionStage.RETEST,
                        RejectionReason.OTHER, TalentCategory.NOT_SUITABLE, null, null, 15));

        assertEquals(409, exception.getStatus().value());
        verify(recruitmentRejectionDao, never()).insert(any());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void duplicateDeclineInTerminalStateReturnsConflict() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.DECLINED));
        when(recruitmentRejectionDao.selectByRecordUuid("record-1"))
                .thenReturn(rejection(3L, "record-1", "DECLINE"));

        ApiException exception = assertThrows(ApiException.class,
                () -> rejectionService.decline("record-1", TalentCategory.NOT_SUITABLE, null, 15));

        assertEquals(409, exception.getStatus().value());
        verify(recruitmentRejectionDao, never()).insert(any());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void findTalentPoolPassesFiltersAndMapsResults() {
        RecruitmentRejectionPo stored = rejection(3L, "record-1", "REJECT");
        stored.setTalentCategory(TalentCategory.FUTURE_CONSIDER.getCode());
        stored.setRejectionReason(RejectionReason.SKILL_MISMATCH.getCode());
        stored.setRejectionStage(RejectionStage.RETEST.getCode());
        when(recruitmentRejectionDao.selectTalentPool(TalentCategory.FUTURE_CONSIDER.getCode(),
                RejectionReason.SKILL_MISMATCH.getCode(), RejectionStage.RETEST.getCode()))
                .thenReturn(List.of(stored));

        List<RecruitmentRejectionVO> result = rejectionService.findTalentPool(
                TalentCategory.FUTURE_CONSIDER, RejectionReason.SKILL_MISMATCH, RejectionStage.RETEST);

        assertEquals(1, result.size());
        assertEquals("record-1", result.get(0).getRecordUuid());
        assertEquals(TalentCategory.FUTURE_CONSIDER, result.get(0).getTalentCategory());
    }

    @Test
    void duplicateKeyDuringInsertReturnsConflict() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_INITIAL));
        when(recruitmentRejectionDao.selectByRecordUuid("record-1")).thenReturn(null);
        when(recruitmentRejectionDao.insert(any(RecruitmentRejectionPo.class)))
                .thenThrow(new DuplicateKeyException("uk_recruitment_rejection_record_uuid"));

        ApiException exception = assertThrows(ApiException.class,
                () -> rejectionService.reject("record-1", RejectionStage.INITIAL_INTERVIEW,
                        RejectionReason.OTHER, TalentCategory.NOT_SUITABLE, null, null, 15));

        assertEquals(409, exception.getStatus().value());
    }

    private static RecruitmentInfoPo recruitment(String recordUuid, RecruitmentStatus status) {
        RecruitmentInfoPo result = new RecruitmentInfoPo();
        result.setRecordUuid(recordUuid);
        result.setStatus(status.getCode());
        return result;
    }

    private static RecruitmentInfoVO recruitmentVo(String recordUuid, RecruitmentStatus status) {
        RecruitmentInfoVO result = new RecruitmentInfoVO();
        result.setRecordUuid(recordUuid);
        result.setStatus(status.getCode());
        return result;
    }

    private static RecruitmentRejectionPo rejection(Long id, String recordUuid, String action) {
        RecruitmentRejectionPo result = new RecruitmentRejectionPo();
        result.setId(id);
        result.setRecordUuid(recordUuid);
        result.setAction(action);
        result.setRejectionTime(LocalDateTime.now());
        return result;
    }
}
