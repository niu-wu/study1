package com.example.study11.service.impl;

import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.dao.RecruitmentStatusHistoryDao;
import com.example.study11.entity.dto.RecruitmentStatusTransitionDTO;
import com.example.study11.entity.enums.RecruitmentStatus;
import com.example.study11.entity.enums.StatusTransitionAction;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.RecruitmentStatusHistoryPo;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.entity.vo.RecruitmentStatusHistoryVO;
import com.example.study11.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruitmentStatusServiceImplTest {

    @Mock
    private RecruitmentInfoDao recruitmentInfoDao;

    @Mock
    private RecruitmentStatusHistoryDao recruitmentStatusHistoryDao;

    @InjectMocks
    private RecruitmentStatusServiceImpl recruitmentStatusService;

    @Test
    void transitionMovesToNextStatusAndRecordsAuthenticatedOperator() {
        RecruitmentInfoPo current = recruitment("record-1", RecruitmentStatus.PENDING_INITIAL);
        when(recruitmentInfoDao.selectByRecordUuid("record-1")).thenReturn(current).thenAnswer(invocation -> {
            current.setStatus(RecruitmentStatus.RETEST_REVIEW.getCode());
            return current;
        });
        when(recruitmentInfoDao.updateStatusIfCurrent(
                "record-1", RecruitmentStatus.PENDING_INITIAL.getCode(),
                RecruitmentStatus.RETEST_REVIEW.getCode())).thenReturn(1);
        when(recruitmentStatusHistoryDao.insert(any(RecruitmentStatusHistoryPo.class))).thenReturn(1);

        RecruitmentStatusTransitionDTO request = new RecruitmentStatusTransitionDTO();
        request.setAction(StatusTransitionAction.SUBMIT_RETEST_REVIEW);
        request.setRemark("初试通过");

        RecruitmentInfoVO result = recruitmentStatusService.transition("record-1", request, 15);

        assertEquals(RecruitmentStatus.RETEST_REVIEW.getCode(), result.getStatus());
        ArgumentCaptor<RecruitmentStatusHistoryPo> historyCaptor =
                ArgumentCaptor.forClass(RecruitmentStatusHistoryPo.class);
        verify(recruitmentStatusHistoryDao).insert(historyCaptor.capture());
        RecruitmentStatusHistoryPo history = historyCaptor.getValue();
        assertEquals("record-1", history.getRecordUuid());
        assertEquals(RecruitmentStatus.PENDING_INITIAL.getCode(), history.getFromStatus());
        assertEquals(RecruitmentStatus.RETEST_REVIEW.getCode(), history.getToStatus());
        assertEquals(StatusTransitionAction.SUBMIT_RETEST_REVIEW.getCode(), history.getAction());
        assertEquals(15, history.getOperatorUserId());
        assertEquals("初试通过", history.getRemark());
        assertNotNull(history.getCreatedAt());
    }

    @Test
    void transitionRejectsIllegalJumpWithoutChangingStatus() {
        RecruitmentInfoPo current = recruitment("record-1", RecruitmentStatus.PENDING_INITIAL);
        when(recruitmentInfoDao.selectByRecordUuid("record-1")).thenReturn(current);

        RecruitmentStatusTransitionDTO request = new RecruitmentStatusTransitionDTO();
        request.setAction(StatusTransitionAction.COMPLETE_RETEST);

        ApiException exception = assertThrows(ApiException.class,
                () -> recruitmentStatusService.transition("record-1", request, 15));

        assertEquals(422, exception.getStatus().value());
    }

    @Test
    void transitionRejectsRepeatedActionFromTerminalStatus() {
        RecruitmentInfoPo current = recruitment("record-1", RecruitmentStatus.REJECTED);
        when(recruitmentInfoDao.selectByRecordUuid("record-1")).thenReturn(current);

        RecruitmentStatusTransitionDTO request = new RecruitmentStatusTransitionDTO();
        request.setAction(StatusTransitionAction.REJECT);

        ApiException exception = assertThrows(ApiException.class,
                () -> recruitmentStatusService.transition("record-1", request, 15));

        assertEquals(422, exception.getStatus().value());
    }

    @Test
    void transitionRejectsMissingOperator() {
        RecruitmentStatusTransitionDTO request = new RecruitmentStatusTransitionDTO();
        request.setAction(StatusTransitionAction.REJECT);

        ApiException exception = assertThrows(ApiException.class,
                () -> recruitmentStatusService.transition("record-1", request, null));

        assertEquals(401, exception.getStatus().value());
    }

    @Test
    void transitionFailsWhenHistoryCannotBePersisted() {
        RecruitmentInfoPo current = recruitment("record-1", RecruitmentStatus.PENDING_INITIAL);
        when(recruitmentInfoDao.selectByRecordUuid("record-1")).thenReturn(current);
        when(recruitmentInfoDao.updateStatusIfCurrent(
                eq("record-1"), eq(RecruitmentStatus.PENDING_INITIAL.getCode()),
                eq(RecruitmentStatus.RETEST_REVIEW.getCode()))).thenReturn(1);
        when(recruitmentStatusHistoryDao.insert(any(RecruitmentStatusHistoryPo.class))).thenReturn(0);

        RecruitmentStatusTransitionDTO request = new RecruitmentStatusTransitionDTO();
        request.setAction(StatusTransitionAction.SUBMIT_RETEST_REVIEW);

        ApiException exception = assertThrows(ApiException.class,
                () -> recruitmentStatusService.transition("record-1", request, 15));

        assertEquals(500, exception.getStatus().value());
    }

    @Test
    void findHistoryRejectsMissingRecruitmentWithoutQueryingHistory() {
        when(recruitmentInfoDao.selectByRecordUuid("missing-record")).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class,
                () -> recruitmentStatusService.findHistory("missing-record"));

        assertEquals(404, exception.getStatus().value());
        verifyNoInteractions(recruitmentStatusHistoryDao);
    }

    @Test
    void findHistoryPreservesAscendingIdOrderFromMapperAndMapsAllFields() {
        RecruitmentInfoPo current = recruitment("record-1", RecruitmentStatus.PENDING_INITIAL);
        RecruitmentStatusHistoryPo first = history(11L, "record-1",
                RecruitmentStatus.PENDING_INITIAL.getCode(), RecruitmentStatus.RETEST_REVIEW.getCode(),
                StatusTransitionAction.SUBMIT_RETEST_REVIEW.getCode(), 15, "passed initial interview",
                LocalDateTime.of(2026, 8, 24, 9, 30));
        RecruitmentStatusHistoryPo second = history(12L, "record-1",
                RecruitmentStatus.RETEST_REVIEW.getCode(), RecruitmentStatus.PENDING_RETEST.getCode(),
                StatusTransitionAction.CONFIRM_RETEST.getCode(), 26, "scheduled retest",
                LocalDateTime.of(2026, 8, 25, 14, 0));
        List<RecruitmentStatusHistoryPo> historyFromMapperInAscendingIdOrder = List.of(first, second);
        when(recruitmentInfoDao.selectByRecordUuid("record-1")).thenReturn(current);
        when(recruitmentStatusHistoryDao.selectByRecordUuid("record-1"))
                .thenReturn(historyFromMapperInAscendingIdOrder);

        List<RecruitmentStatusHistoryVO> result = recruitmentStatusService.findHistory("record-1");

        assertEquals(2, result.size());
        assertHistoryVo(first, result.get(0));
        assertHistoryVo(second, result.get(1));
    }

    private static RecruitmentInfoPo recruitment(String recordUuid, RecruitmentStatus status) {
        RecruitmentInfoPo result = new RecruitmentInfoPo();
        result.setRecordUuid(recordUuid);
        result.setStatus(status.getCode());
        return result;
    }

    private static RecruitmentStatusHistoryPo history(Long id, String recordUuid, String fromStatus,
                                                       String toStatus, String action, Integer operatorUserId,
                                                       String remark, LocalDateTime createdAt) {
        RecruitmentStatusHistoryPo result = new RecruitmentStatusHistoryPo();
        result.setId(id);
        result.setRecordUuid(recordUuid);
        result.setFromStatus(fromStatus);
        result.setToStatus(toStatus);
        result.setAction(action);
        result.setOperatorUserId(operatorUserId);
        result.setRemark(remark);
        result.setCreatedAt(createdAt);
        return result;
    }

    private static void assertHistoryVo(RecruitmentStatusHistoryPo expected,
                                        RecruitmentStatusHistoryVO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getRecordUuid(), actual.getRecordUuid());
        assertEquals(expected.getFromStatus(), actual.getFromStatus());
        assertEquals(expected.getToStatus(), actual.getToStatus());
        assertEquals(expected.getAction(), actual.getAction());
        assertEquals(expected.getOperatorUserId(), actual.getOperatorUserId());
        assertEquals(expected.getRemark(), actual.getRemark());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    }
}
