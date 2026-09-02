package com.example.study11.service.impl;

import com.example.study11.dao.RecruitmentJobDao;
import com.example.study11.dao.RecruitmentJobStatusHistoryDao;
import com.example.study11.entity.dto.RecruitmentJobCreateDTO;
import com.example.study11.entity.enums.UserRole;
import com.example.study11.entity.po.RecruitmentJobPo;
import com.example.study11.service.RoleAuthorizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruitmentJobServiceImplTest {

    @Mock
    private RecruitmentJobDao recruitmentJobDao;

    @Mock
    private RoleAuthorizationService roleAuthorizationService;

    @Mock
    private RecruitmentJobStatusHistoryDao statusHistoryDao;

    @InjectMocks
    private RecruitmentJobServiceImpl service;

    @Test
    void createsJobWithBackendStatusAndSanitizedDescription() {
        RecruitmentJobCreateDTO request = new RecruitmentJobCreateDTO();
        request.setJobName("Java 工程师");
        request.setJobDescription("<p>开发</p><script>alert(1)</script>");
        when(recruitmentJobDao.insert(any(RecruitmentJobPo.class))).thenAnswer(invocation -> {
            RecruitmentJobPo po = invocation.getArgument(0);
            po.setId(10L);
            return 1;
        });
        service.create(request, 2);

        ArgumentCaptor<RecruitmentJobPo> captor = ArgumentCaptor.forClass(RecruitmentJobPo.class);
        verify(recruitmentJobDao).insert(captor.capture());
        RecruitmentJobPo saved = captor.getValue();
        assertNotNull(saved.getJobUuid());
        assertEquals("OPEN", saved.getStatus());
        assertEquals(0, saved.getIsDeleted());
        org.junit.jupiter.api.Assertions.assertFalse(saved.getJobDescription().contains("script"));
        verify(roleAuthorizationService).requireAnyRole(2, UserRole.HR, UserRole.ADMIN);
    }

    @Test
    void rejectsDeleteWhenJobAlreadyDeleted() {
        RecruitmentJobPo current = new RecruitmentJobPo();
        current.setJobUuid("job-1");
        current.setIsDeleted(1);
        when(recruitmentJobDao.selectByJobUuid("job-1")).thenReturn(current);

        assertThrows(RuntimeException.class, () -> service.delete("job-1", 2));
    }
}
