package com.example.study11.mini.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 签到教育经历。 */
@Data
public class MiniSigninEducationPo {

    private Long id;
    private String signinUuid;
    private String startDate;
    private String endDate;
    private String schoolName;
    private String education;
    private String major;
    private Integer sortOrder;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private Integer updatedBy;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
