package com.example.study11.service;

import com.example.study11.entity.vo.RecruitmentCompanyOptionVO;

import java.util.List;

/** 招聘公司业务接口。 */
public interface RecruitmentCompanyService {

    List<RecruitmentCompanyOptionVO> findActiveOptions(Integer operatorUserId);
}
