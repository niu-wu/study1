package com.example.study11.dao;

import com.example.study11.entity.po.EmployeeAssignmentRecordPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 员工稼动事件数据访问层。 */
public interface EmployeeAssignmentRecordDao {

    int insert(EmployeeAssignmentRecordPo recordPo);

    List<EmployeeAssignmentRecordPo> selectByEmployeeUuid(@Param("employeeUuid") String employeeUuid);
}
