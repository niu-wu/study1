package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 岗位和公司配额状态审计持久化对象。 */
@Data
public class RecruitmentJobStatusHistoryPo {

    private Long id;
    private String jobUuid;
    private String allocationUuid;
    private String scope;
    private String fromStatus;
    private String toStatus;
    private String action;
    private Integer operatorUserId;
    private String remark;
    private LocalDateTime createdAt;
}
