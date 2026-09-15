package com.example.study11.dao;

import com.example.study11.entity.po.EmployeeFamilyPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 员工家庭成员数据访问层。 */
public interface EmployeeFamilyDao {

    int insert(EmployeeFamilyPo familyPo);

    List<EmployeeFamilyPo> selectByEmployeeUuid(@Param("employeeUuid") String employeeUuid);

    int deleteByEmployeeUuid(@Param("employeeUuid") String employeeUuid);
}
