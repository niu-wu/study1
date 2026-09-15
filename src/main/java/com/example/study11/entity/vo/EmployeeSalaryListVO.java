package com.example.study11.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 员工月度薪资列表。不含考勤/加班/调休/请假/出差流水。 */
@Data
public class EmployeeSalaryListVO {

    private List<EmployeeSalaryRecordVO> records = new ArrayList<>();

    private BigDecimal salaryTotal;

    private BigDecimal overtimePayTotal;

    private long recordCount;

    /** 应出勤/实出勤/请假天数为 HR 手工快照，不来源于考勤。 */
    private boolean dayCountsAreManualSnapshots = true;

    /** 本接口不返回考勤申请流水。 */
    private boolean attendanceFlowsExcluded = true;
}
