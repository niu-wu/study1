package com.example.study11.entity.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 招聘信息持久化对象。
 *
 * <p>{@code recordUuid} 是招聘记录业务主键，{@code id} 仅由数据库生成并用于排序，
 * 不作为业务身份。</p>
 */
@Data
public class RecruitmentInfoPo {

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
