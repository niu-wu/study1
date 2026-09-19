package com.example.study11.entity.vo;

import com.example.study11.entity.enums.InterviewType;
import lombok.Data;

import java.time.LocalDateTime;

/** 员工面谈记录列表项。 */
@Data
public class EmployeeInterviewVO {

    private String interviewUuid;

    private String employeeUuid;

    private InterviewType interviewType;

    private String interviewTypeLabel;

    private LocalDateTime interviewTime;

    private String content;

    private String handlerName;

    private LocalDateTime createdAt;
}
