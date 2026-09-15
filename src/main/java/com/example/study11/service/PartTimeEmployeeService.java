package com.example.study11.service;

import com.example.study11.entity.dto.EmployeeOnboardingSaveRequest;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.entity.vo.PartTimeEmployeeCreateVO;
import org.springframework.web.multipart.MultipartFile;

/** 项目经理兼职人员登记。 */
public interface PartTimeEmployeeService {

    PartTimeEmployeeCreateVO create(EmployeeOnboardingSaveRequest request, Integer operatorUserId);

    EmployeeOnboardingFormVO findByEmployeeUuid(String employeeUuid, Integer operatorUserId);

    EmployeeOnboardingFormVO uploadPhoto(String employeeUuid, MultipartFile file, Integer operatorUserId);

    EmployeePhotoFileVO loadPhoto(String employeeUuid, Integer operatorUserId);
}
