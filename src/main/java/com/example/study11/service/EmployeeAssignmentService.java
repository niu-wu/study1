package com.example.study11.service;

import com.example.study11.entity.vo.EmployeeAssignmentConfirmVO;
import com.example.study11.entity.vo.EmployeeAssignmentListItemVO;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;

import java.util.List;

/** 人员分配：HR 审核已提交的正式员工登记表。 */
public interface EmployeeAssignmentService {

    List<EmployeeAssignmentListItemVO> listPending(Integer operatorUserId);

    EmployeeOnboardingFormVO getDetail(String employeeUuid, Integer operatorUserId);

    EmployeePhotoFileVO loadPhoto(String employeeUuid, Integer operatorUserId);

    EmployeeAssignmentConfirmVO confirm(String employeeUuid, Integer operatorUserId);
}
