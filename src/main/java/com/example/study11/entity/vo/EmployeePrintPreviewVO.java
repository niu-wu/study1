package com.example.study11.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 员工档案打印预览聚合数据。 */
@Data
public class EmployeePrintPreviewVO {

    /** 个人信息（含 header、基本字段、教育/工作/培训经历）。 */
    private EmployeeArchiveDetailVO basicInfo;

    /** 月度薪资记录。 */
    private List<EmployeeSalaryRecordVO> salaries = new ArrayList<>();

    /** 系统账号。 */
    private List<EmployeeSystemAccountVO> accounts = new ArrayList<>();

    /** 岗位移动记录。 */
    private List<EmployeeAssignmentRecordVO> assignments = new ArrayList<>();

    /** 面谈记录。 */
    private List<EmployeeInterviewVO> interviews = new ArrayList<>();

    /** 合同/证件附件。 */
    private List<EmployeeContractAttachmentVO> attachments = new ArrayList<>();
}
