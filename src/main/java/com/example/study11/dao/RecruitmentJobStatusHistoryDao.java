package com.example.study11.dao;

import com.example.study11.entity.po.RecruitmentJobStatusHistoryPo;

/** 岗位状态审计数据访问层。 */
public interface RecruitmentJobStatusHistoryDao {

    int insert(RecruitmentJobStatusHistoryPo history);
}
