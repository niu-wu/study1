package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 复试安排审核持久化对象。 */
@Data
public class RetestReviewPo {

    private Long id;

    private String recordUuid;

    private String retestCompany;

    private String retestContactPerson;

    private LocalDateTime retestTime;

    private Integer reviewerUserId;

    private LocalDateTime reviewTime;

    private LocalDateTime createdAt;
}
