package com.example.study11.dao;

import com.example.study11.entity.po.RecruitmentJobCompanyPo;
import org.apache.ibatis.annotations.Param;

/** 岗位公司配额数据访问层。 */
public interface RecruitmentJobCompanyDao {

    RecruitmentJobCompanyPo selectByUuidForUpdate(@Param("jobUuid") String jobUuid,
                                                    @Param("allocationUuid") String allocationUuid);

    int updateStatusIfCurrent(@Param("jobUuid") String jobUuid,
                              @Param("allocationUuid") String allocationUuid,
                              @Param("fromStatus") String fromStatus,
                              @Param("toStatus") String toStatus,
                              @Param("operatorUserId") Integer operatorUserId);
}
