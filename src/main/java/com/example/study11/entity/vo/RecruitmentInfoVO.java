package com.example.study11.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 招聘信息响应对象，不暴露数据库内部迁移字段。 */
@Data
public class RecruitmentInfoVO {

    /** 当前分页结果中的展示序号，非数据库主键。 */
    private Long serialNo;

    private String recordUuid;

    private Long id;

    private String applicantName;

    private String gender;

    private String position;

    private String phone;

    private String email;

    private String applicationChannel;

    private String applicationMethod;

    private String status;

    private String initialContactPerson;

    private LocalDateTime initialInterviewTime;

    private String retestContactPerson;

    private LocalDateTime retestInterviewTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
