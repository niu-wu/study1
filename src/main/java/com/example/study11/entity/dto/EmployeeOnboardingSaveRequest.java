package com.example.study11.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 入职登记草稿保存请求。身份字段和表单状态由服务端维护。 */
@Data
public class EmployeeOnboardingSaveRequest {

    @Size(max = 50, message = "姓名长度不能超过50")
    private String fullName;

    @Size(max = 10, message = "性别长度不能超过10")
    private String gender;

    private LocalDate birthDate;

    @Size(max = 100, message = "职位长度不能超过100")
    private String position;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    private String email;

    @Size(max = 18, message = "身份证号长度不能超过18")
    private String idCard;

    @Size(max = 20, message = "婚姻状况长度不能超过20")
    private String maritalStatus;

    @Size(max = 30, message = "政治面貌长度不能超过30")
    private String politicalStatus;

    @Size(max = 50, message = "国籍长度不能超过50")
    private String nationality;

    @Size(max = 30, message = "民族长度不能超过30")
    private String ethnicity;

    @Size(max = 100, message = "籍贯长度不能超过100")
    private String nativePlace;

    @Size(max = 200, message = "户口所在地长度不能超过200")
    private String hukouLocation;

    @Size(max = 200, message = "现住地址长度不能超过200")
    private String currentAddress;

    @Size(max = 10, message = "邮编长度不能超过10")
    private String postalCode;

    @Size(max = 50, message = "健康状况长度不能超过50")
    private String healthStatus;

    @Size(max = 50, message = "最高学历长度不能超过50")
    private String highestEducation;

    @Size(max = 100, message = "专业长度不能超过100")
    private String major;

    @Size(max = 100, message = "专业职称长度不能超过100")
    private String professionalTitle;

    @Size(max = 100, message = "外语及等级长度不能超过100")
    private String foreignLanguage;

    @Size(max = 200, message = "爱好特长长度不能超过200")
    private String hobbies;

    @Size(max = 500, message = "照片路径长度不能超过500")
    private String photoPath;

    @Size(max = 64, message = "微信号长度不能超过64")
    private String wechatAccount;

    @Size(max = 20, message = "学历经历不能超过20条")
    private List<@Valid EmployeeEducationItemDTO> educations = new ArrayList<>();

    @Size(max = 20, message = "工作经历不能超过20条")
    private List<@Valid EmployeeWorkHistoryItemDTO> workHistories = new ArrayList<>();

    @Size(max = 20, message = "培训经历不能超过20条")
    private List<@Valid EmployeeTrainingItemDTO> trainings = new ArrayList<>();

    @Size(max = 20, message = "家庭成员不能超过20条")
    private List<@Valid EmployeeFamilyItemDTO> familyMembers = new ArrayList<>();

    @Size(max = 20, message = "紧急联络人不能超过20条")
    private List<@Valid EmployeeEmergencyContactItemDTO> emergencyContacts = new ArrayList<>();
}
