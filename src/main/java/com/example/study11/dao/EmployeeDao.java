package com.example.study11.dao;

import com.example.study11.entity.dto.EmployeeArchivePageRequest;
import com.example.study11.entity.po.EmployeeArchiveStatisticsPo;
import com.example.study11.entity.po.EmployeePo;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 员工入职登记主档数据访问层。 */
public interface EmployeeDao {

    int insert(EmployeePo employeePo);

    EmployeePo selectByUserId(@Param("userId") Integer userId);

    EmployeePo selectByUserIdForUpdate(@Param("userId") Integer userId);

    EmployeePo selectByEmployeeUuid(@Param("employeeUuid") String employeeUuid);

    EmployeePo selectByEmployeeUuidForUpdate(@Param("employeeUuid") String employeeUuid);

    List<EmployeePo> selectPendingAssignments();

    int updateByEmployeeUuid(EmployeePo employeePo);

    int confirmAssignment(@Param("employeeUuid") String employeeUuid,
                          @Param("hrConfirmedAt") LocalDateTime hrConfirmedAt,
                          @Param("hrConfirmedBy") Integer hrConfirmedBy);

    long countConfirmedArchives(@Param("request") EmployeeArchivePageRequest request);

    List<EmployeePo> selectConfirmedArchives(@Param("request") EmployeeArchivePageRequest request,
                                             @Param("offset") long offset,
                                             @Param("limit") int limit);

    EmployeeArchiveStatisticsPo selectArchiveStatistics();

    int updateArchiveFields(EmployeePo employeePo);

    int updateAssignmentState(EmployeePo employeePo);

    int regularize(@Param("employeeUuid") String employeeUuid,
                   @Param("regularizedAt") LocalDate regularizedAt,
                   @Param("updatedAt") LocalDateTime updatedAt);
}
