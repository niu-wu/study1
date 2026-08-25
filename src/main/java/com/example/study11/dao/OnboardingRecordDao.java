package com.example.study11.dao;

import com.example.study11.entity.po.OnboardingRecordPo;
import org.apache.ibatis.annotations.Param;

/** 入职关联记录数据访问层。 */
public interface OnboardingRecordDao {

    int insert(OnboardingRecordPo onboardingRecordPo);

    OnboardingRecordPo selectByRecordUuid(@Param("recordUuid") String recordUuid);
}
