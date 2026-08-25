package com.example.study11.dao;

import com.example.study11.entity.po.RecruitmentRejectionPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 淘汰与人才库数据访问层。 */
public interface RecruitmentRejectionDao {

    int insert(RecruitmentRejectionPo rejectionPo);

    RecruitmentRejectionPo selectByRecordUuid(@Param("recordUuid") String recordUuid);

    List<RecruitmentRejectionPo> selectTalentPool(@Param("talentCategory") String talentCategory,
                                                  @Param("rejectionReason") String rejectionReason,
                                                  @Param("rejectionStage") String rejectionStage);
}
