package com.example.study11.dao;

import com.example.study11.entity.po.EmployeeContractAttachmentPo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 员工合同/证件附件数据访问层。 */
public interface EmployeeContractAttachmentDao {

    int insert(EmployeeContractAttachmentPo recordPo);

    EmployeeContractAttachmentPo selectByUuid(@Param("attachmentUuid") String attachmentUuid);

    List<EmployeeContractAttachmentPo> selectByEmployeeUuid(@Param("employeeUuid") String employeeUuid);

    int updateByUuid(EmployeeContractAttachmentPo recordPo);

    int deleteByUuid(@Param("attachmentUuid") String attachmentUuid, @Param("updatedBy") Integer updatedBy);
}
