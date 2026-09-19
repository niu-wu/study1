package com.example.study11.service;

import com.example.study11.common.model.PageResult;
import com.example.study11.entity.dto.EmployeeArchivePageRequest;
import com.example.study11.entity.dto.EmployeeArchiveUpdateRequest;
import com.example.study11.entity.dto.EmployeeAssignmentSaveRequest;
import com.example.study11.entity.dto.EmployeeInterviewSaveRequest;
import com.example.study11.entity.dto.EmployeeSalarySaveRequest;
import com.example.study11.entity.dto.EmployeeSystemAccountSaveRequest;
import com.example.study11.entity.vo.EmployeeArchiveDetailVO;
import com.example.study11.entity.vo.EmployeeArchiveListItemVO;
import com.example.study11.entity.vo.EmployeeArchiveStatisticsVO;
import com.example.study11.entity.vo.EmployeeAssignmentListVO;
import com.example.study11.entity.vo.EmployeeAssignmentRecordVO;
import com.example.study11.entity.vo.EmployeeInterviewVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.entity.vo.EmployeeSalaryListVO;
import com.example.study11.entity.vo.EmployeeSalaryRecordVO;
import com.example.study11.entity.vo.EmployeeSystemAccountVO;

import java.util.List;

/** 已确认员工档案列表、详情、统计、有限 PATCH、月度薪资、稼动、系统账号和面谈记录。 */
public interface EmployeeArchiveService {

    PageResult<EmployeeArchiveListItemVO> findPage(EmployeeArchivePageRequest request, Integer operatorUserId);

    EmployeeArchiveStatisticsVO findStatistics(Integer operatorUserId);

    EmployeeArchiveListItemVO update(String employeeUuid, EmployeeArchiveUpdateRequest request,
                                     Integer operatorUserId);

    EmployeeArchiveDetailVO getDetail(String employeeUuid, Integer operatorUserId);

    EmployeePhotoFileVO loadPhoto(String employeeUuid, Integer operatorUserId);

    EmployeeSalaryRecordVO saveSalary(String employeeUuid, EmployeeSalarySaveRequest request,
                                      Integer operatorUserId);

    EmployeeSalaryListVO listSalaries(String employeeUuid, Integer operatorUserId);

    EmployeeAssignmentRecordVO saveAssignment(String employeeUuid, EmployeeAssignmentSaveRequest request,
                                              Integer operatorUserId);

    EmployeeAssignmentListVO listAssignments(String employeeUuid, Integer operatorUserId);

    EmployeeSystemAccountVO saveAccount(String employeeUuid, EmployeeSystemAccountSaveRequest request,
                                        Integer operatorUserId);

    List<EmployeeSystemAccountVO> listAccounts(String employeeUuid, Integer operatorUserId);

    EmployeeInterviewVO saveInterview(String employeeUuid, EmployeeInterviewSaveRequest request,
                                      Integer operatorUserId);

    List<EmployeeInterviewVO> listInterviews(String employeeUuid, Integer operatorUserId);
}
