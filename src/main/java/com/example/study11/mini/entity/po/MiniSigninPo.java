package com.example.study11.mini.entity.po;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 应聘者面试签到主表。 */
@Data
public class MiniSigninPo {

    private String signinUuid;
    private Long id;
    private Long positionId;
    private String positionName;
    private String name;
    private String gender;
    private String phone;
    private String email;
    private String idCard;
    private LocalDate birthDate;
    private String address;
    private String applyChannel;
    private String referrer;
    private String interviewMode;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private Integer updatedBy;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
