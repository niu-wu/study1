package com.example.study11.dao;

import com.example.study11.entity.po.EmployeeEducationPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 员工学历经历数据访问层。 */
public interface EmployeeEducationDao {

    int insert(EmployeeEducationPo educationPo);

    List<EmployeeEducationPo> selectByEmployeeUuid(@Param("employeeUuid") String employeeUuid);

    int deleteByEmployeeUuid(@Param("employeeUuid") String employeeUuid);
}
