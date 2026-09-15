package com.example.study11.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 岗位稼动履历和汇总。 */
@Data
public class EmployeeAssignmentListVO {

    private List<EmployeeAssignmentRecordVO> records = new ArrayList<>();

    private EmployeeAssignmentSummaryVO summary = new EmployeeAssignmentSummaryVO();
}
