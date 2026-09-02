package com.example.study11.service.impl;

import com.example.study11.dao.RecruitmentCompanyDao;
import com.example.study11.entity.po.RecruitmentCompanyPo;
import com.example.study11.entity.enums.UserRole;
import com.example.study11.service.RoleAuthorizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruitmentCompanyServiceImplTest {

    @Mock
    private RecruitmentCompanyDao recruitmentCompanyDao;
    @Mock
    private RoleAuthorizationService roleAuthorizationService;
    @InjectMocks
    private RecruitmentCompanyServiceImpl service;

    @Test
    void returnsOnlyActiveCompanyOptionsForHr() {
        RecruitmentCompanyPo company = new RecruitmentCompanyPo();
        company.setCompanyUuid("company-1");
        company.setCompanyName("示例公司");
        company.setCurrentHeadcount(3);
        when(recruitmentCompanyDao.findActiveOptions()).thenReturn(List.of(company));

        var result = service.findActiveOptions(2);

        assertEquals("company-1", result.get(0).getCompanyUuid());
        assertEquals(3, result.get(0).getCurrentHeadcount());
        verify(roleAuthorizationService).requireAnyRole(2, UserRole.HR, UserRole.ADMIN);
    }
}
