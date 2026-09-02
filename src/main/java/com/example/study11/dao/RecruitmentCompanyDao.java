package com.example.study11.dao;

import com.example.study11.entity.po.RecruitmentCompanyPo;

import java.util.List;

/** 招聘公司数据访问层。 */
public interface RecruitmentCompanyDao {

    List<RecruitmentCompanyPo> findActiveOptions();
}
