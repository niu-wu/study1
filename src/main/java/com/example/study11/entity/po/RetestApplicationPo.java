package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 复试申请持久化对象。 */
@Data
public class RetestApplicationPo {

    private Long id;

    private String recordUuid;

    private LocalDateTime applicationTime;

    private String applicantRemark;

    private Integer applicantUserId;

    private LocalDateTime createdAt;
}
