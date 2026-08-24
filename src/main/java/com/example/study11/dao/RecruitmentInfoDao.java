package com.example.study11.dao;

import com.example.study11.entity.po.RecruitmentInfoPo;
import com.example.study11.entity.po.RecruitmentInfoStatisticsPo;
import com.example.study11.entity.dto.RecruitmentInfoPageRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 招聘信息数据访问层。 */
public interface RecruitmentInfoDao {

    int insert(RecruitmentInfoPo recruitmentInfoPo);

    RecruitmentInfoPo selectByRecordUuid(@Param("recordUuid") String recordUuid);

    RecruitmentInfoPo selectByRecordUuidForUpdate(@Param("recordUuid") String recordUuid);

    RecruitmentInfoPo selectById(@Param("id") Long id);

    List<RecruitmentInfoPo> findList(@Param("applicantName") String applicantName,
                                     @Param("position") String position,
                                     @Param("status") String status);

    long countByCondition(@Param("request") RecruitmentInfoPageRequest request);

    List<RecruitmentInfoPo> selectPage(@Param("request") RecruitmentInfoPageRequest request,
                                       @Param("offset") long offset,
                                       @Param("limit") int limit);

    RecruitmentInfoStatisticsPo selectStatistics();

    int updateStatusIfCurrent(@Param("recordUuid") String recordUuid,
                               @Param("fromStatus") String fromStatus,
                               @Param("toStatus") String toStatus);

    int updateByRecordUuid(RecruitmentInfoPo recruitmentInfoPo);

    int deleteByRecordUuid(@Param("recordUuid") String recordUuid);
}
