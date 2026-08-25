package com.example.study11.service;

import com.example.study11.entity.enums.RejectionReason;
import com.example.study11.entity.enums.RejectionStage;
import com.example.study11.entity.enums.TalentCategory;
import com.example.study11.entity.vo.RecruitmentRejectionVO;

import java.util.List;

/** 淘汰、放弃入职和人才库业务接口。 */
public interface RejectionService {

    RecruitmentRejectionVO reject(String recordUuid, RejectionStage rejectionStage,
                                  RejectionReason rejectionReason, TalentCategory talentCategory,
                                  String remark, Long resumeAttachmentId, Integer operatorUserId);

    RecruitmentRejectionVO decline(String recordUuid, TalentCategory talentCategory,
                                   String remark, Integer operatorUserId);

    List<RecruitmentRejectionVO> findTalentPool(TalentCategory talentCategory,
                                                RejectionReason rejectionReason,
                                                RejectionStage rejectionStage);
}
