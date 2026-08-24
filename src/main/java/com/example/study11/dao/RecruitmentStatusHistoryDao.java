package com.example.study11.dao;

import com.example.study11.entity.po.RecruitmentStatusHistoryPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 招聘状态历史数据访问层。 */
public interface RecruitmentStatusHistoryDao {

    int insert(RecruitmentStatusHistoryPo historyPo);

    long countByRecordUuid(@Param("recordUuid") String recordUuid);

    List<RecruitmentStatusHistoryPo> selectByRecordUuid(@Param("recordUuid") String recordUuid);
}
