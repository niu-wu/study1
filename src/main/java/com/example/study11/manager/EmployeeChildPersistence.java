package com.example.study11.manager;

import com.example.study11.dao.EmployeeEducationDao;
import com.example.study11.dao.EmployeeEmergencyContactDao;
import com.example.study11.dao.EmployeeFamilyDao;
import com.example.study11.dao.EmployeeTrainingDao;
import com.example.study11.dao.EmployeeWorkHistoryDao;
import com.example.study11.entity.dto.EmployeeEducationItemDTO;
import com.example.study11.entity.dto.EmployeeEmergencyContactItemDTO;
import com.example.study11.entity.dto.EmployeeFamilyItemDTO;
import com.example.study11.entity.dto.EmployeeOnboardingSaveRequest;
import com.example.study11.entity.dto.EmployeeTrainingItemDTO;
import com.example.study11.entity.dto.EmployeeWorkHistoryItemDTO;
import com.example.study11.entity.po.EmployeeEducationPo;
import com.example.study11.entity.po.EmployeeEmergencyContactPo;
import com.example.study11.entity.po.EmployeeFamilyPo;
import com.example.study11.entity.po.EmployeeTrainingPo;
import com.example.study11.entity.po.EmployeeWorkHistoryPo;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/** 入职登记一对多表整表替换。 */
@Component
public class EmployeeChildPersistence {

    private final EmployeeEducationDao employeeEducationDao;

    private final EmployeeWorkHistoryDao employeeWorkHistoryDao;

    private final EmployeeTrainingDao employeeTrainingDao;

    private final EmployeeFamilyDao employeeFamilyDao;

    private final EmployeeEmergencyContactDao employeeEmergencyContactDao;

    public EmployeeChildPersistence(EmployeeEducationDao employeeEducationDao,
                                    EmployeeWorkHistoryDao employeeWorkHistoryDao,
                                    EmployeeTrainingDao employeeTrainingDao,
                                    EmployeeFamilyDao employeeFamilyDao,
                                    EmployeeEmergencyContactDao employeeEmergencyContactDao) {
        this.employeeEducationDao = employeeEducationDao;
        this.employeeWorkHistoryDao = employeeWorkHistoryDao;
        this.employeeTrainingDao = employeeTrainingDao;
        this.employeeFamilyDao = employeeFamilyDao;
        this.employeeEmergencyContactDao = employeeEmergencyContactDao;
    }

    public void replaceAll(String employeeUuid, EmployeeOnboardingSaveRequest request, LocalDateTime now) {
        employeeEducationDao.deleteByEmployeeUuid(employeeUuid);
        employeeWorkHistoryDao.deleteByEmployeeUuid(employeeUuid);
        employeeTrainingDao.deleteByEmployeeUuid(employeeUuid);
        employeeFamilyDao.deleteByEmployeeUuid(employeeUuid);
        employeeEmergencyContactDao.deleteByEmployeeUuid(employeeUuid);
        if (request == null) {
            return;
        }
        insertEducations(employeeUuid, request.getEducations(), now);
        insertWorkHistories(employeeUuid, request.getWorkHistories(), now);
        insertTrainings(employeeUuid, request.getTrainings(), now);
        insertFamilyMembers(employeeUuid, request.getFamilyMembers(), now);
        insertEmergencyContacts(employeeUuid, request.getEmergencyContacts(), now);
    }

    private void insertEducations(String employeeUuid, List<EmployeeEducationItemDTO> items,
                                  LocalDateTime now) {
        if (items == null) {
            return;
        }
        int sortNo = 0;
        for (EmployeeEducationItemDTO item : items) {
            if (item == null) {
                continue;
            }
            EmployeeEducationPo po = new EmployeeEducationPo();
            po.setEmployeeUuid(employeeUuid);
            po.setSortNo(item.getSortNo() == null ? sortNo : item.getSortNo());
            po.setStartDate(item.getStartDate());
            po.setEndDate(item.getEndDate());
            po.setSchoolName(trimToNull(item.getSchoolName()));
            po.setMajor(trimToNull(item.getMajor()));
            po.setEducationLevel(trimToNull(item.getEducationLevel()));
            po.setCertificate(trimToNull(item.getCertificate()));
            po.setCreatedAt(now);
            po.setUpdatedAt(now);
            employeeEducationDao.insert(po);
            sortNo++;
        }
    }

    private void insertWorkHistories(String employeeUuid, List<EmployeeWorkHistoryItemDTO> items,
                                     LocalDateTime now) {
        if (items == null) {
            return;
        }
        int sortNo = 0;
        for (EmployeeWorkHistoryItemDTO item : items) {
            if (item == null) {
                continue;
            }
            EmployeeWorkHistoryPo po = new EmployeeWorkHistoryPo();
            po.setEmployeeUuid(employeeUuid);
            po.setSortNo(item.getSortNo() == null ? sortNo : item.getSortNo());
            po.setStartDate(item.getStartDate());
            po.setEndDate(item.getEndDate());
            po.setCompanyName(trimToNull(item.getCompanyName()));
            po.setPosition(trimToNull(item.getPosition()));
            po.setLeaveReason(trimToNull(item.getLeaveReason()));
            po.setReferenceName(trimToNull(item.getReferenceName()));
            po.setReferencePhone(trimToNull(item.getReferencePhone()));
            po.setCreatedAt(now);
            po.setUpdatedAt(now);
            employeeWorkHistoryDao.insert(po);
            sortNo++;
        }
    }

    private void insertTrainings(String employeeUuid, List<EmployeeTrainingItemDTO> items,
                                 LocalDateTime now) {
        if (items == null) {
            return;
        }
        int sortNo = 0;
        for (EmployeeTrainingItemDTO item : items) {
            if (item == null) {
                continue;
            }
            EmployeeTrainingPo po = new EmployeeTrainingPo();
            po.setEmployeeUuid(employeeUuid);
            po.setSortNo(item.getSortNo() == null ? sortNo : item.getSortNo());
            po.setStartDate(item.getStartDate());
            po.setEndDate(item.getEndDate());
            po.setInstitution(trimToNull(item.getInstitution()));
            po.setCourseContent(trimToNull(item.getCourseContent()));
            po.setTrainingResult(trimToNull(item.getTrainingResult()));
            po.setCreatedAt(now);
            po.setUpdatedAt(now);
            employeeTrainingDao.insert(po);
            sortNo++;
        }
    }

    private void insertFamilyMembers(String employeeUuid, List<EmployeeFamilyItemDTO> items,
                                     LocalDateTime now) {
        if (items == null) {
            return;
        }
        int sortNo = 0;
        for (EmployeeFamilyItemDTO item : items) {
            if (item == null) {
                continue;
            }
            EmployeeFamilyPo po = new EmployeeFamilyPo();
            po.setEmployeeUuid(employeeUuid);
            po.setSortNo(item.getSortNo() == null ? sortNo : item.getSortNo());
            po.setFullName(trimToNull(item.getFullName()));
            po.setRelationship(trimToNull(item.getRelationship()));
            po.setWorkUnit(trimToNull(item.getWorkUnit()));
            po.setJobTitle(trimToNull(item.getJobTitle()));
            po.setCreatedAt(now);
            po.setUpdatedAt(now);
            employeeFamilyDao.insert(po);
            sortNo++;
        }
    }

    private void insertEmergencyContacts(String employeeUuid,
                                         List<EmployeeEmergencyContactItemDTO> items,
                                         LocalDateTime now) {
        if (items == null) {
            return;
        }
        int sortNo = 0;
        for (EmployeeEmergencyContactItemDTO item : items) {
            if (item == null) {
                continue;
            }
            EmployeeEmergencyContactPo po = new EmployeeEmergencyContactPo();
            po.setEmployeeUuid(employeeUuid);
            po.setSortNo(item.getSortNo() == null ? sortNo : item.getSortNo());
            po.setFullName(trimToNull(item.getFullName()));
            po.setRelationship(trimToNull(item.getRelationship()));
            po.setAddress(trimToNull(item.getAddress()));
            po.setPostalCode(trimToNull(item.getPostalCode()));
            po.setPhone(trimToNull(item.getPhone()));
            po.setCreatedAt(now);
            po.setUpdatedAt(now);
            employeeEmergencyContactDao.insert(po);
            sortNo++;
        }
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
