package com.example.study11.dao;

import com.example.study11.entity.po.EmployeeWorkHistoryPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 员工工作经历数据访问层。 */
public interface EmployeeWorkHistoryDao {

    int insert(EmployeeWorkHistoryPo workHistoryPo);

    List<EmployeeWorkHistoryPo> selectByEmployeeUuid(@Param("employeeUuid") String employeeUuid);

    int deleteByEmployeeUuid(@Param("employeeUuid") String employeeUuid);
}
