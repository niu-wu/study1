package com.example.study11.entity.vo;

import com.example.study11.entity.enums.RejectionReason;
import com.example.study11.entity.enums.RejectionStage;
import com.example.study11.entity.enums.TalentCategory;
import lombok.Data;

import java.time.LocalDateTime;

/** 淘汰与人才库记录响应对象。 */
@Data
public class RecruitmentRejectionVO {

    private Long id;

    private String recordUuid;

    private String action;

    private RejectionStage rejectionStage;

    private RejectionReason rejectionReason;

    private String remark;

    private Long resumeAttachmentId;

    private TalentCategory talentCategory;

    private Integer operatorUserId;

    private LocalDateTime rejectionTime;

    private LocalDateTime createdAt;

    private String status;
}
