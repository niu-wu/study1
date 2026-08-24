package com.example.study11.entity.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** 招聘信息分页和组合筛选请求。 */
@Data
public class RecruitmentInfoPageRequest {

    @Min(value = 1, message = "页码必须大于等于1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量必须大于等于1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 20;

    private String applicantName;

    /** 同时匹配手机号和邮箱。 */
    private String phoneOrEmailKeyword;

    private String position;

    private String status;

    private String initialContactPerson;

    private String retestContactPerson;

    private String applicationChannel;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTo;
}
