package com.example.study11.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/** 招聘信息资料更新请求，不包含状态、主键和迁移字段。 */
@Data
public class RecruitmentInfoUpdateDTO {

    @NotBlank(message = "应聘人姓名不能为空")
    @Size(max = 50, message = "应聘人姓名长度不能超过50")
    private String applicantName;

    @Size(max = 10, message = "性别长度不能超过10")
    private String gender;

    @NotBlank(message = "岗位不能为空")
    @Size(max = 100, message = "岗位长度不能超过100")
    private String position;

    private String jobUuid;

    private String jobCompanyAllocationUuid;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱长度不能超过255")
    private String email;

    @Size(max = 50, message = "应聘渠道长度不能超过50")
    private String applicationChannel;

    @Size(max = 50, message = "应聘方式长度不能超过50")
    private String applicationMethod;

    @Size(max = 50, message = "初试对接人长度不能超过50")
    private String initialContactPerson;

    private LocalDateTime initialInterviewTime;

    @Size(max = 50, message = "复试对接人长度不能超过50")
    private String retestContactPerson;

    private LocalDateTime retestInterviewTime;
}
