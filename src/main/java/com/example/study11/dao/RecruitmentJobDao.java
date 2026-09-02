package com.example.study11.dao;

import com.example.study11.entity.dto.RecruitmentJobPageRequest;
import com.example.study11.entity.po.RecruitmentJobPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 招聘岗位数据访问层。 */
public interface RecruitmentJobDao {

    int insert(RecruitmentJobPo job);

    RecruitmentJobPo selectByJobUuid(@Param("jobUuid") String jobUuid);

    RecruitmentJobPo selectByJobUuidForUpdate(@Param("jobUuid") String jobUuid);

    List<RecruitmentJobPo> selectPage(@Param("request") RecruitmentJobPageRequest request,
                                      @Param("offset") long offset,
                                      @Param("limit") int limit);

    long countByCondition(@Param("request") RecruitmentJobPageRequest request);

    int updateByJobUuid(RecruitmentJobPo job);

    int updateStatusIfCurrent(@Param("jobUuid") String jobUuid,
                               @Param("fromStatus") String fromStatus,
                               @Param("toStatus") String toStatus,
                               @Param("operatorUserId") Integer operatorUserId);

    int softDelete(@Param("jobUuid") String jobUuid,
                   @Param("operatorUserId") Integer operatorUserId);
}
