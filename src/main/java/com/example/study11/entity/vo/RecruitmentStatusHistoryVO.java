package com.example.study11.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 招聘状态历史响应对象。 */
@Data
public class RecruitmentStatusHistoryVO {

    private Long id;

    private String recordUuid;

    private String fromStatus;

    private String toStatus;

    private String action;

    private Integer operatorUserId;

    private String remark;

    private LocalDateTime createdAt;
}
