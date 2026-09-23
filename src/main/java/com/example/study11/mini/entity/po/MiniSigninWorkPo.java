package com.example.study11.mini.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 签到工作经历。 */
@Data
public class MiniSigninWorkPo {

    private Long id;
    private String signinUuid;
    private String startDate;
    private String endDate;
    private String companyName;
    private String position;
    private String leaveReason;
    private Integer sortOrder;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private Integer updatedBy;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
