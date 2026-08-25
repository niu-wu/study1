package com.example.study11.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 复试安排审核响应对象。 */
@Data
public class RetestReviewVO {

    private Long id;

    private String recordUuid;

    private String retestCompany;

    private String retestContactPerson;

    private LocalDateTime retestTime;

    private Integer reviewerUserId;

    private LocalDateTime reviewTime;

    private LocalDateTime createdAt;
}
