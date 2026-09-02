package com.example.study11.service.impl;

import com.example.study11.dao.RecruitmentCompanyDao;
import com.example.study11.entity.enums.UserRole;
import com.example.study11.entity.vo.RecruitmentCompanyOptionVO;
import com.example.study11.service.RecruitmentCompanyService;
import com.example.study11.service.RoleAuthorizationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/** 招聘公司业务实现。 */
@Service
public class RecruitmentCompanyServiceImpl implements RecruitmentCompanyService {

    private final RecruitmentCompanyDao recruitmentCompanyDao;
    private final RoleAuthorizationService roleAuthorizationService;

    public RecruitmentCompanyServiceImpl(RecruitmentCompanyDao recruitmentCompanyDao,
                                         RoleAuthorizationService roleAuthorizationService) {
        this.recruitmentCompanyDao = recruitmentCompanyDao;
        this.roleAuthorizationService = roleAuthorizationService;
    }

    @Override
    public List<RecruitmentCompanyOptionVO> findActiveOptions(Integer operatorUserId) {
        roleAuthorizationService.requireAnyRole(operatorUserId, UserRole.HR, UserRole.ADMIN);
        return recruitmentCompanyDao.findActiveOptions().stream().map(source -> {
            RecruitmentCompanyOptionVO result = new RecruitmentCompanyOptionVO();
            result.setCompanyUuid(source.getCompanyUuid());
            result.setCompanyName(source.getCompanyName());
            result.setCurrentHeadcount(source.getCurrentHeadcount());
            return result;
        }).collect(Collectors.toList());
    }
}
