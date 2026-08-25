package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 入职关联记录持久化对象，不保存初始密码。 */
@Data
public class OnboardingRecordPo {

    private Long id;

    private String recordUuid;

    private Integer userId;

    private LocalDate onboardingDate;

    private String onboardingNote;

    private Integer processedByUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 仅用于服务层向一次性响应传递密码，不映射数据库字段。 */
    private transient String initialPassword;
}
