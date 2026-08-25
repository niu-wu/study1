package com.example.study11.entity.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 入职关联记录响应对象。初始密码仅在办理成功响应中返回一次。 */
@Data
public class OnboardingRecordVO {

    private Long id;

    private String recordUuid;

    private String status;

    private Integer userId;

    private String username;

    private LocalDate onboardingDate;

    private String onboardingNote;

    private Integer processedByUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String initialPassword;
}
