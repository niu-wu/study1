package com.example.study11.dao;

import com.example.study11.entity.po.CandidateResumePo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 候选人简历附件数据访问层。 */
public interface CandidateResumeDao {

    int insert(CandidateResumePo candidateResumePo);

    CandidateResumePo selectById(@Param("id") Long id);

    CandidateResumePo selectByIdForUpdate(@Param("id") Long id);

    List<CandidateResumePo> selectByRecordUuid(@Param("recordUuid") String recordUuid);

    long countByRecordUuid(@Param("recordUuid") String recordUuid);

    int deleteById(@Param("id") Long id);
}
