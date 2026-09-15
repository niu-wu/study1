package com.example.study11.dao;

import com.example.study11.entity.po.EmployeeSalaryRecordPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 员工月度薪资快照数据访问层。 */
public interface EmployeeSalaryRecordDao {

    int insert(EmployeeSalaryRecordPo recordPo);

    List<EmployeeSalaryRecordPo> selectByEmployeeUuid(@Param("employeeUuid") String employeeUuid);
}
