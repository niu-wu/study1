package com.example.study11.service.impl;

import com.example.study11.dao.OnboardingRecordDao;
import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.entity.dto.RecruitmentStatusTransitionDTO;
import com.example.study11.entity.dto.UserSaveDTO;
import com.example.study11.entity.enums.RecruitmentStatus;
import com.example.study11.entity.enums.StatusTransitionAction;
import com.example.study11.entity.po.OnboardingRecordPo;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.UserPo;
import com.example.study11.entity.vo.OnboardingRecordVO;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.RecruitmentStatusService;
import com.example.study11.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceImplTest {

    @Mock
    private RecruitmentInfoDao recruitmentInfoDao;

    @Mock
    private OnboardingRecordDao onboardingRecordDao;

    @Mock
    private UserService userService;

    @Mock
    private RecruitmentStatusService recruitmentStatusService;

    @InjectMocks
    private OnboardingServiceImpl onboardingService;

    @Test
    void processCreatesStudy11UserAndOnboardingRecordAndReturnsOneTimePassword() {
        LocalDate onboardingDate = LocalDate.of(2026, 9, 1);
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_ONBOARDING,
                        "13800138000", "candidate@example.com", "候选人"));
        when(onboardingRecordDao.selectByRecordUuid("record-1")).thenReturn(null);
        when(userService.getByUserName("13800138000")).thenReturn(null);
        when(userService.dealSave(any(UserSaveDTO.class))).thenReturn(51);
        when(onboardingRecordDao.insert(any(OnboardingRecordPo.class))).thenAnswer(invocation -> {
            OnboardingRecordPo record = invocation.getArgument(0);
            record.setId(9L);
            return 1;
        });
        when(recruitmentStatusService.transition(eq("record-1"), any(RecruitmentStatusTransitionDTO.class),
                eq(15))).thenReturn(recruitmentVo("record-1", RecruitmentStatus.ONBOARDED));
        OnboardingRecordPo saved = onboarding(9L, "record-1", 51, onboardingDate, 15);
        when(onboardingRecordDao.selectByRecordUuid("record-1")).thenReturn(null, saved);

        OnboardingRecordVO result = onboardingService.process("record-1", onboardingDate,
                "正式入职", 15);

        ArgumentCaptor<UserSaveDTO> userCaptor = ArgumentCaptor.forClass(UserSaveDTO.class);
        verify(userService).dealSave(userCaptor.capture());
        assertEquals("13800138000", userCaptor.getValue().getUsername());
        assertEquals("13800138000", userCaptor.getValue().getPhone());
        assertEquals("candidate@example.com", userCaptor.getValue().getEmail());
        assertNotNull(userCaptor.getValue().getPassword());
        assertEquals(51, result.getUserId());
        assertEquals("13800138000", result.getUsername());
        assertEquals(onboardingDate, result.getOnboardingDate());
        assertEquals("正式入职", result.getOnboardingNote());
        assertNotNull(result.getInitialPassword());
        assertEquals(8, result.getInitialPassword().length());

        ArgumentCaptor<OnboardingRecordPo> recordCaptor = ArgumentCaptor.forClass(OnboardingRecordPo.class);
        verify(onboardingRecordDao).insert(recordCaptor.capture());
        assertEquals("record-1", recordCaptor.getValue().getRecordUuid());
        assertEquals(51, recordCaptor.getValue().getUserId());
        assertEquals(15, recordCaptor.getValue().getProcessedByUserId());
        assertEquals(onboardingDate, recordCaptor.getValue().getOnboardingDate());
        assertNull(recordCaptor.getValue().getInitialPassword());

        ArgumentCaptor<RecruitmentStatusTransitionDTO> transitionCaptor =
                ArgumentCaptor.forClass(RecruitmentStatusTransitionDTO.class);
        verify(recruitmentStatusService).transition(eq("record-1"), transitionCaptor.capture(), eq(15));
        assertEquals(StatusTransitionAction.COMPLETE_ONBOARDING, transitionCaptor.getValue().getAction());
        assertEquals(RecruitmentStatus.ONBOARDED.getCode(), result.getStatus());
    }

    @Test
    void processRejectsAccountNameConflictBeforeCreatingRecord() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_ONBOARDING,
                        "13800138000", "candidate@example.com", "候选人"));
        when(onboardingRecordDao.selectByRecordUuid("record-1")).thenReturn(null);
        UserPo existing = new UserPo();
        existing.setId(88);
        when(userService.getByUserName("13800138000")).thenReturn(existing);

        ApiException exception = assertThrows(ApiException.class,
                () -> onboardingService.process("record-1", null, null, 15));

        assertEquals(409, exception.getStatus().value());
        verify(userService, never()).dealSave(any());
        verify(onboardingRecordDao, never()).insert(any());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void processRejectsDuplicateOnboardingBeforeCreatingAccount() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_ONBOARDING,
                        "13800138000", null, "候选人"));
        when(onboardingRecordDao.selectByRecordUuid("record-1"))
                .thenReturn(onboarding(9L, "record-1", 51, null, 15));

        ApiException exception = assertThrows(ApiException.class,
                () -> onboardingService.process("record-1", null, null, 15));

        assertEquals(409, exception.getStatus().value());
        verifyNoInteractions(userService, recruitmentStatusService);
    }

    @Test
    void processRequiresPendingOnboardingStatus() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_RETEST,
                        "13800138000", null, "候选人"));
        when(onboardingRecordDao.selectByRecordUuid("record-1")).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> onboardingService.process("record-1", null, null, 15));

        assertEquals(422, exception.getStatus().value());
        verifyNoInteractions(userService, recruitmentStatusService);
    }

    @Test
    void processRequiresCandidatePhone() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_ONBOARDING,
                        null, "candidate@example.com", "候选人"));
        when(onboardingRecordDao.selectByRecordUuid("record-1")).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> onboardingService.process("record-1", null, null, 15));

        assertEquals(400, exception.getStatus().value());
        verifyNoInteractions(userService, recruitmentStatusService);
    }

    @Test
    void processRollsBackLaterStepsWhenOnboardingInsertFails() {
        when(recruitmentInfoDao.selectByRecordUuidForUpdate("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.PENDING_ONBOARDING,
                        "13800138000", null, "候选人"));
        when(onboardingRecordDao.selectByRecordUuid("record-1")).thenReturn(null);
        when(userService.getByUserName("13800138000")).thenReturn(null);
        when(userService.dealSave(any(UserSaveDTO.class))).thenReturn(51);
        when(onboardingRecordDao.insert(any(OnboardingRecordPo.class))).thenReturn(0);

        ApiException exception = assertThrows(ApiException.class,
                () -> onboardingService.process("record-1", null, null, 15));

        assertEquals(500, exception.getStatus().value());
        verifyNoInteractions(recruitmentStatusService);
    }

    @Test
    void findReturnsDetailsWithoutInitialPassword() {
        when(recruitmentInfoDao.selectByRecordUuid("record-1"))
                .thenReturn(recruitment("record-1", RecruitmentStatus.ONBOARDED,
                        "13800138000", "candidate@example.com", "候选人"));
        when(onboardingRecordDao.selectByRecordUuid("record-1"))
                .thenReturn(onboarding(9L, "record-1", 51, LocalDate.of(2026, 9, 1), 15));
        UserPo user = new UserPo();
        user.setId(51);
        user.setUsername("13800138000");
        when(userService.getByUserName("13800138000")).thenReturn(user);

        OnboardingRecordVO result = onboardingService.findByRecordUuid("record-1");

        assertEquals(51, result.getUserId());
        assertEquals("13800138000", result.getUsername());
        assertNull(result.getInitialPassword());
    }

    private static RecruitmentInfoPo recruitment(String recordUuid, RecruitmentStatus status,
                                                  String phone, String email, String applicantName) {
        RecruitmentInfoPo result = new RecruitmentInfoPo();
        result.setRecordUuid(recordUuid);
        result.setStatus(status.getCode());
        result.setPhone(phone);
        result.setEmail(email);
        result.setApplicantName(applicantName);
        return result;
    }

    private static RecruitmentInfoVO recruitmentVo(String recordUuid, RecruitmentStatus status) {
        RecruitmentInfoVO result = new RecruitmentInfoVO();
        result.setRecordUuid(recordUuid);
        result.setStatus(status.getCode());
        return result;
    }

    private static OnboardingRecordPo onboarding(Long id, String recordUuid, Integer userId,
                                                 LocalDate onboardingDate, Integer processedByUserId) {
        OnboardingRecordPo result = new OnboardingRecordPo();
        result.setId(id);
        result.setRecordUuid(recordUuid);
        result.setUserId(userId);
        result.setOnboardingDate(onboardingDate);
        result.setProcessedByUserId(processedByUserId);
        result.setCreatedAt(LocalDateTime.now());
        return result;
    }
}
