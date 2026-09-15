package com.example.study11.dao;

import com.example.study11.entity.po.EmployeeEmergencyContactPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 员工紧急联络人数据访问层。 */
public interface EmployeeEmergencyContactDao {

    int insert(EmployeeEmergencyContactPo contactPo);

    List<EmployeeEmergencyContactPo> selectByEmployeeUuid(@Param("employeeUuid") String employeeUuid);

    int deleteByEmployeeUuid(@Param("employeeUuid") String employeeUuid);
}
