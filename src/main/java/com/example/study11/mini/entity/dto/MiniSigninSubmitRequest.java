package com.example.study11.mini.entity.dto;

import com.example.study11.mini.entity.enums.ApplyChannel;
import com.example.study11.mini.entity.enums.InterviewMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/** 应聘者签到提交请求。 */
@Data
public class MiniSigninSubmitRequest {

    private Long positionId;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名过长")
    private String name;

    private String gender;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 18, message = "身份证号过长")
    private String idCard;

    private LocalDate birthDate;

    @Size(max = 200, message = "地址过长")
    private String address;

    @NotNull(message = "应聘渠道不能为空")
    private ApplyChannel applyChannel;

    @Size(max = 50, message = "对接人姓名过长")
    private String referrer;

    @NotNull(message = "面试方式不能为空")
    private InterviewMode interviewMode;

    @Valid
    private List<EducationItem> educations;

    @Valid
    private List<WorkItem> works;

    @Data
    public static class EducationItem {
        private String startDate;
        private String endDate;
        private String schoolName;
        private String education;
        private String major;
    }

    @Data
    public static class WorkItem {
        private String startDate;
        private String endDate;
        private String companyName;
        private String position;
        private String leaveReason;
    }
}
