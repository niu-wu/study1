package com.example.study11.entity.po;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 招聘岗位持久化对象。 */
@Data
public class RecruitmentJobPo {

    private String jobUuid;
    private Long id;
    private String jobName;
    private String jobCode;
    private String jobDescription;
    private String recruitmentRequirements;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String workLocation;
    private String status;
    private Integer isDeleted;
    private Integer createdByUserId;
    private Integer updatedByUserId;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
