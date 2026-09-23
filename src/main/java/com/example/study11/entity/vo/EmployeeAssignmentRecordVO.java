package com.example.study11.entity.vo;

import com.example.study11.entity.enums.AssignmentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 稼动事件履历。时段和时长由相邻事件推导，不落库。 */
@Data
public class EmployeeAssignmentRecordVO {

    private String assignmentUuid;

    private AssignmentType assignmentType;

    private LocalDate eventDate;

    private String companyName;

    private BigDecimal utilizationRate;

    /** 经办人登录名。用户表没有姓名列。 */
    private String operatorName;

    /** 结束日减开始日，不含结束日当天。进行中用今天。 */
    private Integer utilizationDays;

    private LocalDate startDate;

    private LocalDate endDate;

    private String durationText;

    private boolean inProgress;

    private String remark;
}
