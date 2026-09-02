package com.example.study11.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 招聘岗位响应对象。 */
@Data
public class RecruitmentJobVO {

    private Long serialNo;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
