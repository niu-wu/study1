package com.example.study11.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 复试申请响应对象。 */
@Data
public class RetestApplicationVO {

    private Long id;

    private String recordUuid;

    private LocalDateTime applicationTime;

    private String applicantRemark;

    private Integer applicantUserId;

    private LocalDateTime createdAt;
}
