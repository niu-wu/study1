package com.example.study11.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

/** 稼动汇总。ENTER 条数为项目数，进行中计入累计时长。 */
@Data
public class EmployeeAssignmentSummaryVO {

    private long projectCount;

    private long inProgressCount;

    private String accumulatedDurationText;

    /** 稼动率按稼动天数加权。天数合计为 0 时为空。 */
    private BigDecimal accumulatedUtilizationRate;
}
