package com.example.study11.dao;

import com.example.study11.entity.po.EmployeeTrainingPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 员工培训经历数据访问层。 */
public interface EmployeeTrainingDao {

    int insert(EmployeeTrainingPo trainingPo);

    List<EmployeeTrainingPo> selectByEmployeeUuid(@Param("employeeUuid") String employeeUuid);

    int deleteByEmployeeUuid(@Param("employeeUuid") String employeeUuid);
}
