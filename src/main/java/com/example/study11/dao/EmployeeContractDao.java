package com.example.study11.dao;

import com.example.study11.entity.po.EmployeeContractPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 员工劳动合同数据访问层。 */
public interface EmployeeContractDao {

    int insert(EmployeeContractPo recordPo);

    EmployeeContractPo selectByUuid(@Param("contractUuid") String contractUuid);

    List<EmployeeContractPo> selectByEmployeeUuid(@Param("employeeUuid") String employeeUuid);

    int updateByUuid(EmployeeContractPo recordPo);

    int deleteByUuid(@Param("contractUuid") String contractUuid, @Param("updatedBy") Integer updatedBy);
}
