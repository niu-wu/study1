package com.example.study11.dao;

import com.example.study11.entity.po.EmployeeSystemAccountPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 员工系统账号数据访问层。 */
public interface EmployeeSystemAccountDao {

    int insert(EmployeeSystemAccountPo accountPo);

    List<EmployeeSystemAccountPo> selectByEmployeeUuid(@Param("employeeUuid") String employeeUuid);
}
