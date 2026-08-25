package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 淘汰与人才库记录持久化对象。 */
@Data
public class RecruitmentRejectionPo {

    private Long id;

    private String recordUuid;

    private String action;

    private String rejectionStage;

    private String rejectionReason;

    private String remark;

    private Long resumeAttachmentId;

    private String talentCategory;

    private Integer operatorUserId;

    private LocalDateTime rejectionTime;

    private LocalDateTime createdAt;
}
