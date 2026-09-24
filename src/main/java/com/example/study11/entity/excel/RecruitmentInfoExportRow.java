package com.example.study11.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

/** 招聘信息导出 Excel 行。 */
@Data
@ColumnWidth(20)
public class RecruitmentInfoExportRow {

    @ExcelProperty("序号")
    private Long serialNo;

    @ExcelProperty("应聘人姓名")
    private String applicantName;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("岗位")
    private String position;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("应聘渠道")
    private String applicationChannel;

    @ExcelProperty("应聘方式")
    private String applicationMethod;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("初试对接人")
    private String initialContactPerson;

    @ExcelProperty("初试时间")
    private LocalDateTime initialInterviewTime;

    @ExcelProperty("复试对接人")
    private String retestContactPerson;

    @ExcelProperty("复试时间")
    private LocalDateTime retestInterviewTime;
}
