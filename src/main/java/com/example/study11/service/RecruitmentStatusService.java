package com.example.study11.service;

import com.example.study11.entity.dto.RecruitmentStatusTransitionDTO;
import com.example.study11.entity.vo.RecruitmentInfoVO;
import com.example.study11.entity.vo.RecruitmentStatusHistoryVO;

import java.util.List;

/** 招聘状态流转业务接口。 */
public interface RecruitmentStatusService {

    RecruitmentInfoVO transition(String recordUuid, RecruitmentStatusTransitionDTO request,
                                  Integer operatorUserId);

    List<RecruitmentStatusHistoryVO> findHistory(String recordUuid);
}
