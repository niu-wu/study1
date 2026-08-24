package com.example.study11.service.impl;

import com.example.study11.common.model.PageResult;
import com.example.study11.dao.RecruitmentInfoDao;
import com.example.study11.entity.dto.RecruitmentInfoPageRequest;
import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.RecruitmentInfoStatisticsPo;
import com.example.study11.entity.vo.RecruitmentInfoStatisticsVO;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruitmentInfoPaginationServiceTest {

    @Mock
    private RecruitmentInfoDao recruitmentInfoDao;

    @InjectMocks
    private RecruitmentInfoServiceImpl recruitmentInfoService;

    @Test
    void pageUsesStableCrossPageSerialNumbers() {
        RecruitmentInfoPageRequest request = new RecruitmentInfoPageRequest();
        request.setPage(2);
        request.setPageSize(2);

        RecruitmentInfoPo first = po(18L, "record-18");
        RecruitmentInfoPo second = po(17L, "record-17");
        when(recruitmentInfoDao.countByCondition(request)).thenReturn(5L);
        when(recruitmentInfoDao.selectPage(eq(request), eq(2L), eq(2))).thenReturn(List.of(first, second));

        PageResult<RecruitmentInfoVO> result = recruitmentInfoService.findPage(request);

        assertEquals(5L, result.getTotal());
        assertEquals(3, result.getTotalPages());
        assertEquals(3L, result.getRecords().get(0).getSerialNo());
        assertEquals(4L, result.getRecords().get(1).getSerialNo());
        verify(recruitmentInfoDao).selectPage(request, 2L, 2);
    }

    @Test
    void pageRejectsInvalidDateRange() {
        RecruitmentInfoPageRequest request = new RecruitmentInfoPageRequest();
        request.setCreatedFrom(LocalDateTime.of(2026, 8, 24, 0, 0));
        request.setCreatedTo(LocalDateTime.of(2026, 8, 23, 0, 0));

        assertThrows(ApiException.class, () -> recruitmentInfoService.findPage(request));
    }

    @Test
    void statisticsMapsAllBuckets() {
        RecruitmentInfoStatisticsPo statistics = new RecruitmentInfoStatisticsPo();
        statistics.setPendingInitial(2L);
        statistics.setPendingRetest(3L);
        statistics.setPendingOnboarding(4L);
        statistics.setNotPassed(5L);
        when(recruitmentInfoDao.selectStatistics()).thenReturn(statistics);

        RecruitmentInfoStatisticsVO result = recruitmentInfoService.findStatistics();

        assertEquals(2L, result.getPendingInitial());
        assertEquals(3L, result.getPendingRetest());
        assertEquals(4L, result.getPendingOnboarding());
        assertEquals(5L, result.getNotPassed());
    }

    private static RecruitmentInfoPo po(long id, String recordUuid) {
        RecruitmentInfoPo po = new RecruitmentInfoPo();
        po.setId(id);
        po.setRecordUuid(recordUuid);
        po.setApplicantName("候选人" + id);
        po.setPosition("Java工程师");
        po.setStatus("PENDING_INITIAL");
        return po;
    }
}
