package com.example.study11.dao;

import com.example.study11.entity.po.EmployeeInterviewPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 员工面谈记录数据访问层。 */
public interface EmployeeInterviewDao {

    int insert(EmployeeInterviewPo recordPo);

    EmployeeInterviewPo selectByUuid(@Param("interviewUuid") String interviewUuid);

    List<EmployeeInterviewPo> selectByEmployeeUuid(@Param("employeeUuid") String employeeUuid);

    int updateByUuid(EmployeeInterviewPo recordPo);

    int deleteByUuid(@Param("interviewUuid") String interviewUuid);
}
