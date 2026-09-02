package com.example.study11.service;

import com.example.study11.common.model.PageResult;
import com.example.study11.entity.dto.RecruitmentJobCreateDTO;
import com.example.study11.entity.dto.RecruitmentJobPageRequest;
import com.example.study11.entity.dto.RecruitmentJobStatusTransitionDTO;
import com.example.study11.entity.dto.RecruitmentJobUpdateDTO;
import com.example.study11.entity.dto.RecruitmentAllocationStatusTransitionDTO;
import com.example.study11.entity.vo.RecruitmentJobVO;

/** 招聘岗位业务接口。 */
public interface RecruitmentJobService {

    RecruitmentJobVO create(RecruitmentJobCreateDTO request, Integer operatorUserId);

    PageResult<RecruitmentJobVO> findPage(RecruitmentJobPageRequest request, Integer operatorUserId);

    RecruitmentJobVO findByJobUuid(String jobUuid, Integer operatorUserId);

    RecruitmentJobVO update(String jobUuid, RecruitmentJobUpdateDTO request, Integer operatorUserId);

    RecruitmentJobVO transitionStatus(String jobUuid, RecruitmentJobStatusTransitionDTO request,
                                      Integer operatorUserId);

    void transitionAllocationStatus(String jobUuid, String allocationUuid,
                                    RecruitmentAllocationStatusTransitionDTO request,
                                    Integer operatorUserId);

    void delete(String jobUuid, Integer operatorUserId);
}
