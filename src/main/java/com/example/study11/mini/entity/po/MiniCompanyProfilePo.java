package com.example.study11.mini.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/** 公司简介。 */
@Data
public class MiniCompanyProfilePo {

    private Long id;
    private String title;
    private String content;
    private String stat1Value;
    private String stat1Label;
    private String stat2Value;
    private String stat2Label;
    private String stat3Value;
    private String stat3Label;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private Integer updatedBy;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
