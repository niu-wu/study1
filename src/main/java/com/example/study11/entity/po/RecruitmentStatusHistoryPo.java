package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 招聘状态历史持久化对象。 */
@Data
public class RecruitmentStatusHistoryPo {

    private Long id;

    private String recordUuid;

    private String fromStatus;

    private String toStatus;

    private String action;

    private Integer operatorUserId;

    private String remark;

    private LocalDateTime createdAt;
}
