package com.example.study11.service.impl;

import com.example.study11.common.model.PageResult;
import com.example.study11.config.TimeConfig;
import com.example.study11.convert.EmployeeFormAssembler;
import com.example.study11.dao.EmployeeAssignmentRecordDao;
import com.example.study11.dao.EmployeeContractAttachmentDao;
import com.example.study11.dao.EmployeeContractDao;
import com.example.study11.dao.EmployeeDao;
import com.example.study11.dao.EmployeeInterviewDao;
import com.example.study11.dao.EmployeeSalaryRecordDao;
import com.example.study11.dao.EmployeeSystemAccountDao;
import com.example.study11.dao.UserDao;
import com.example.study11.entity.dto.EmployeeArchivePageRequest;
import com.example.study11.entity.dto.EmployeeArchiveUpdateRequest;
import com.example.study11.entity.dto.EmployeeAssignmentSaveRequest;
import com.example.study11.entity.dto.EmployeeContractAttachmentSaveRequest;
import com.example.study11.entity.dto.EmployeeContractSaveRequest;
import com.example.study11.entity.dto.EmployeeInterviewSaveRequest;
import com.example.study11.entity.dto.EmployeeSalarySaveRequest;
import com.example.study11.entity.dto.EmployeeSystemAccountSaveRequest;
import com.example.study11.entity.enums.AssignmentType;
import com.example.study11.entity.enums.EmploymentStatus;
import com.example.study11.entity.enums.EmploymentType;
import com.example.study11.entity.enums.AttachmentType;
import com.example.study11.entity.enums.ContractSignType;
import com.example.study11.entity.enums.ContractStatus;
import com.example.study11.entity.enums.ContractTermType;
import com.example.study11.entity.enums.InterviewType;
import com.example.study11.entity.enums.PrintSection;
import com.example.study11.entity.enums.WorkLocation;
import com.example.study11.entity.po.EmployeeArchiveStatisticsPo;
import com.example.study11.entity.po.EmployeeAssignmentRecordPo;
import com.example.study11.entity.po.EmployeeContractAttachmentPo;
import com.example.study11.entity.po.EmployeeContractPo;
import com.example.study11.entity.po.EmployeeInterviewPo;
import com.example.study11.entity.po.EmployeePo;
import com.example.study11.entity.po.EmployeeSalaryRecordPo;
import com.example.study11.entity.po.EmployeeSystemAccountPo;
import com.example.study11.entity.po.UserPo;
import com.example.study11.entity.vo.EmployeeArchiveDetailVO;
import com.example.study11.entity.vo.EmployeeArchiveHeaderVO;
import com.example.study11.entity.vo.EmployeeArchiveListItemVO;
import com.example.study11.entity.vo.EmployeeArchiveStatisticsVO;
import com.example.study11.entity.vo.EmployeeAssignmentListVO;
import com.example.study11.entity.vo.EmployeeAssignmentRecordVO;
import com.example.study11.entity.vo.EmployeeAssignmentSummaryVO;
import com.example.study11.entity.vo.EmployeeContractAttachmentVO;
import com.example.study11.entity.vo.EmployeeContractVO;
import com.example.study11.entity.vo.EmployeeInterviewVO;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import com.example.study11.entity.vo.EmployeePrintPreviewVO;
import com.example.study11.entity.vo.EmployeeSalaryListVO;
import com.example.study11.entity.vo.EmployeeSalaryRecordVO;
import com.example.study11.entity.vo.EmployeeSystemAccountVO;
import com.example.study11.exception.ApiException;
import com.example.study11.service.EmployeeArchiveService;
import com.example.study11.service.EmployeePhotoStorageService;
import com.example.study11.service.RoleAuthorizationService;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** 已确认员工档案列表、详情、全局统计和有限 PATCH。 */
@Service
public class EmployeeArchiveServiceImpl implements EmployeeArchiveService {

    private final EmployeeDao employeeDao;

    private final RoleAuthorizationService roleAuthorizationService;

    private final UserDao userDao;

    private final EmployeeFormAssembler formAssembler;

    private final EmployeePhotoStorageService employeePhotoStorageService;

    private final EmployeeSalaryRecordDao employeeSalaryRecordDao;

    private final EmployeeAssignmentRecordDao employeeAssignmentRecordDao;

    private final EmployeeSystemAccountDao employeeSystemAccountDao;

    private final EmployeeInterviewDao employeeInterviewDao;

    private final EmployeeContractDao employeeContractDao;

    private final EmployeeContractAttachmentDao employeeContractAttachmentDao;

    private final Clock clock;

    public EmployeeArchiveServiceImpl(EmployeeDao employeeDao,
                                      RoleAuthorizationService roleAuthorizationService,
                                      UserDao userDao,
                                      EmployeeFormAssembler formAssembler,
                                      EmployeePhotoStorageService employeePhotoStorageService,
                                      EmployeeSalaryRecordDao employeeSalaryRecordDao,
                                      EmployeeAssignmentRecordDao employeeAssignmentRecordDao,
                                      EmployeeSystemAccountDao employeeSystemAccountDao,
                                      EmployeeInterviewDao employeeInterviewDao,
                                      EmployeeContractDao employeeContractDao,
                                      EmployeeContractAttachmentDao employeeContractAttachmentDao,
                                      Clock clock) {
        this.employeeDao = employeeDao;
        this.roleAuthorizationService = roleAuthorizationService;
        this.userDao = userDao;
        this.formAssembler = formAssembler;
        this.employeePhotoStorageService = employeePhotoStorageService;
        this.employeeSalaryRecordDao = employeeSalaryRecordDao;
        this.employeeAssignmentRecordDao = employeeAssignmentRecordDao;
        this.employeeSystemAccountDao = employeeSystemAccountDao;
        this.employeeInterviewDao = employeeInterviewDao;
        this.employeeContractDao = employeeContractDao;
        this.employeeContractAttachmentDao = employeeContractAttachmentDao;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<EmployeeArchiveListItemVO> findPage(EmployeeArchivePageRequest request,
                                                         Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        EmployeeArchivePageRequest condition = normalizePageRequest(request);
        int page = condition.getPage();
        int pageSize = condition.getPageSize();
        long offset = Math.multiplyExact((long) page - 1, pageSize);
        long total = employeeDao.countConfirmedArchives(condition);
        if (total == 0 || offset >= total) {
            return new PageResult<>(page, pageSize, total, List.of());
        }
        List<EmployeePo> employees = employeeDao.selectConfirmedArchives(condition, offset, pageSize);
        List<EmployeeArchiveListItemVO> records = new ArrayList<>();
        if (employees != null) {
            for (EmployeePo employee : employees) {
                records.add(toListItem(employee));
            }
        }
        return new PageResult<>(page, pageSize, total, records);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeArchiveStatisticsVO findStatistics(Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        EmployeeArchiveStatisticsPo source = employeeDao.selectArchiveStatistics();
        EmployeeArchiveStatisticsVO result = new EmployeeArchiveStatisticsVO();
        result.setProbationCount(valueOrZero(source == null ? null : source.getProbationCount()));
        result.setActiveCount(valueOrZero(source == null ? null : source.getActiveCount()));
        result.setHeadquartersCount(valueOrZero(source == null ? null : source.getHeadquartersCount()));
        result.setDispatchedCount(valueOrZero(source == null ? null : source.getDispatchedCount()));
        result.setResignedCount(valueOrZero(source == null ? null : source.getResignedCount()));
        return result;
    }

    @Override
    @Transactional
    public EmployeeArchiveListItemVO update(String employeeUuid, EmployeeArchiveUpdateRequest request,
                                            Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        validateEmployeeUuid(employeeUuid);
        if (request == null) {
            throw ApiException.badRequest("档案修正内容不能为空");
        }
        request.rejectUnexpectedFields();
        EmployeePo employee = employeeDao.selectByEmployeeUuidForUpdate(employeeUuid);
        if (employee == null || employee.getHrConfirmedAt() == null) {
            throw ApiException.notFound("员工档案不存在");
        }
        applyAllowedUpdates(employee, request);
        employee.setUpdatedAt(LocalDateTime.now());
        if (employeeDao.updateArchiveFields(employee) != 1) {
            throw ApiException.notFound("员工档案不存在");
        }
        EmployeePo saved = employeeDao.selectByEmployeeUuid(employeeUuid);
        if (saved == null) {
            throw ApiException.internalServerError("员工档案保存失败");
        }
        return toListItem(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeArchiveDetailVO getDetail(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        EmployeePo employee = requireConfirmedArchive(employeeUuid);
        EmployeeOnboardingFormVO form = formAssembler.toFormWithoutProgress(employee);
        EmployeeArchiveDetailVO detail = new EmployeeArchiveDetailVO();
        detail.setHeader(toHeader(employee));
        detail.setEmployeeUuid(employee.getEmployeeUuid());
        if (form != null) {
            detail.setEmail(form.getEmail());
            detail.setIdCard(form.getIdCard());
            detail.setBirthDate(form.getBirthDate());
            detail.setMaritalStatus(form.getMaritalStatus());
            detail.setPoliticalStatus(form.getPoliticalStatus());
            detail.setNationality(form.getNationality());
            detail.setEthnicity(form.getEthnicity());
            detail.setNativePlace(form.getNativePlace());
            detail.setHukouLocation(form.getHukouLocation());
            detail.setCurrentAddress(form.getCurrentAddress());
            detail.setPostalCode(form.getPostalCode());
            detail.setHealthStatus(form.getHealthStatus());
            detail.setHighestEducation(form.getHighestEducation());
            detail.setMajor(form.getMajor());
            detail.setProfessionalTitle(form.getProfessionalTitle());
            detail.setForeignLanguage(form.getForeignLanguage());
            detail.setHobbies(form.getHobbies());
            detail.setWechatAccount(form.getWechatAccount());
            detail.setPhotoUploaded(form.isPhotoUploaded());
            detail.setEducations(form.getEducations());
            detail.setWorkHistories(form.getWorkHistories());
            detail.setTrainings(form.getTrainings());
            detail.setFamilyMembers(form.getFamilyMembers());
            detail.setEmergencyContacts(form.getEmergencyContacts());
        }
        return detail;
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePhotoFileVO loadPhoto(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        EmployeePo employee = requireConfirmedArchive(employeeUuid);
        return employeePhotoStorageService.load(employee.getPhotoPath());
    }

    @Override
    @Transactional
    public EmployeeSalaryRecordVO saveSalary(String employeeUuid, EmployeeSalarySaveRequest request,
                                             Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        if (request == null) {
            throw ApiException.badRequest("薪资内容不能为空");
        }
        request.rejectUnexpectedFields();
        EmployeePo employee = requireConfirmedArchiveForUpdate(employeeUuid);
        EmployeeSalaryRecordPo record = toSalaryRecord(employee.getEmployeeUuid(), request, operatorUserId);
        try {
            if (employeeSalaryRecordDao.insert(record) != 1) {
                throw ApiException.internalServerError("薪资记录保存失败");
            }
        } catch (DuplicateKeyException exception) {
            throw ApiException.conflict("该月薪资已存在");
        }
        return toSalaryVo(record);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeSalaryListVO listSalaries(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        requireConfirmedArchive(employeeUuid);
        List<EmployeeSalaryRecordPo> source = employeeSalaryRecordDao.selectByEmployeeUuid(employeeUuid);
        EmployeeSalaryListVO result = new EmployeeSalaryListVO();
        result.setDayCountsAreManualSnapshots(true);
        result.setAttendanceFlowsExcluded(true);
        if (source == null || source.isEmpty()) {
            result.setRecords(List.of());
            result.setRecordCount(0);
            result.setSalaryTotal(zeroMoney());
            result.setOvertimePayTotal(zeroMoney());
            return result;
        }
        List<EmployeeSalaryRecordVO> records = new ArrayList<>();
        BigDecimal salaryTotal = zeroMoney();
        BigDecimal overtimeTotal = zeroMoney();
        for (EmployeeSalaryRecordPo item : source) {
            records.add(toSalaryVo(item));
            salaryTotal = salaryTotal.add(nullToZero(item.getNetPay()));
            overtimeTotal = overtimeTotal.add(nullToZero(item.getOvertimePay()));
        }
        result.setRecords(records);
        result.setRecordCount(records.size());
        result.setSalaryTotal(salaryTotal);
        result.setOvertimePayTotal(overtimeTotal);
        return result;
    }

    @Override
    @Transactional
    public EmployeeAssignmentRecordVO saveAssignment(String employeeUuid, EmployeeAssignmentSaveRequest request,
                                                     Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        if (request == null) {
            throw ApiException.badRequest("稼动事件不能为空");
        }
        request.rejectUnexpectedFields();
        if (request.getAssignmentType() == null) {
            throw ApiException.badRequest("稼动类型不能为空");
        }
        if (request.getEventDate() == null) {
            throw ApiException.badRequest("事件日期不能为空");
        }
        EmployeePo employee = requireConfirmedArchiveForUpdate(employeeUuid);
        validateAssignmentPayload(request);
        WorkLocation currentLocation = WorkLocation.fromCode(employee.getWorkLocation());
        if (currentLocation == null) {
            currentLocation = WorkLocation.HEADQUARTERS;
        }
        if (request.getAssignmentType() == AssignmentType.ENTER
                && currentLocation != WorkLocation.HEADQUARTERS) {
            throw ApiException.unprocessableEntity("当前状态不允许进入客户");
        }
        if (request.getAssignmentType() == AssignmentType.RETURN
                && currentLocation != WorkLocation.DISPATCHED) {
            throw ApiException.unprocessableEntity("当前状态不允许回公司");
        }
        List<EmployeeAssignmentRecordPo> existing = employeeAssignmentRecordDao.selectByEmployeeUuid(employeeUuid);
        EmployeeAssignmentRecordPo lastEvent = lastEvent(existing);
        if (lastEvent != null && request.getEventDate().isBefore(lastEvent.getEventDate())) {
            throw ApiException.badRequest("事件日期不能早于最后一次稼动");
        }
        EmployeeAssignmentRecordPo record = toAssignmentPo(employeeUuid, request, operatorUserId);
        if (employeeAssignmentRecordDao.insert(record) != 1) {
            throw ApiException.internalServerError("稼动事件保存失败");
        }
        applyAssignmentToEmployee(employee, request);
        if (employeeDao.updateAssignmentState(employee) != 1) {
            throw ApiException.internalServerError("员工档案地点更新失败");
        }
        List<EmployeeAssignmentRecordPo> timeline = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
        timeline.add(record);
        return toAssignmentVos(timeline).get(timeline.size() - 1);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeAssignmentListVO listAssignments(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        requireConfirmedArchive(employeeUuid);
        List<EmployeeAssignmentRecordPo> source = employeeAssignmentRecordDao.selectByEmployeeUuid(employeeUuid);
        List<EmployeeAssignmentRecordVO> records = toAssignmentVos(source);
        EmployeeAssignmentListVO result = new EmployeeAssignmentListVO();
        result.setRecords(records);
        result.setSummary(summarizeAssignments(records));
        return result;
    }

    @Override
    @Transactional
    public EmployeeSystemAccountVO saveAccount(String employeeUuid, EmployeeSystemAccountSaveRequest request,
                                               Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        if (request == null) {
            throw ApiException.badRequest("系统账号不能为空");
        }
        request.rejectUnexpectedFields();
        EmployeePo employee = requireConfirmedArchiveForUpdate(employeeUuid);
        String systemName = trimToNull(request.getSystemName());
        String accountName = trimToNull(request.getAccountName());
        if (systemName == null) {
            throw ApiException.badRequest("系统名称不能为空");
        }
        if (accountName == null) {
            throw ApiException.badRequest("账号名不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        EmployeeSystemAccountPo record = new EmployeeSystemAccountPo();
        record.setAccountUuid(UUID.randomUUID().toString());
        record.setEmployeeUuid(employee.getEmployeeUuid());
        record.setSystemName(systemName);
        record.setAccountName(accountName);
        record.setPassword(trimToNull(request.getPassword()));
        record.setOpenedAt(request.getOpenedAt());
        record.setOperatorUserId(operatorUserId);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        if (employeeSystemAccountDao.insert(record) != 1) {
            throw ApiException.internalServerError("系统账号保存失败");
        }
        return toAccountVo(record, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeSystemAccountVO> listAccounts(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        EmployeePo employee = requireConfirmedArchive(employeeUuid);
        List<EmployeeSystemAccountVO> result = new ArrayList<>();
        result.add(hrSystemAccountRow(employee));
        List<EmployeeSystemAccountPo> stored = employeeSystemAccountDao.selectByEmployeeUuid(employeeUuid);
        if (stored != null) {
            for (EmployeeSystemAccountPo item : stored) {
                result.add(toAccountVo(item, false));
            }
        }
        return result;
    }

    @Override
    @Transactional
    public EmployeeInterviewVO saveInterview(String employeeUuid, EmployeeInterviewSaveRequest request,
                                             Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        if (request == null) {
            throw ApiException.badRequest("面谈记录不能为空");
        }
        request.rejectUnexpectedFields();
        EmployeePo employee = requireConfirmedArchiveForUpdate(employeeUuid);

        String cleanContent = cleanInterviewContent(request.getContent());

        LocalDateTime now = LocalDateTime.now();
        EmployeeInterviewPo record = new EmployeeInterviewPo();
        record.setInterviewUuid(UUID.randomUUID().toString());
        record.setEmployeeUuid(employee.getEmployeeUuid());
        record.setInterviewType(request.getInterviewType().getCode());
        record.setInterviewTime(request.getInterviewTime());
        record.setContent(cleanContent);
        record.setHandlerUserId(operatorUserId);
        record.setCreatedBy(operatorUserId);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        if (employeeInterviewDao.insert(record) != 1) {
            throw ApiException.internalServerError("面谈记录保存失败");
        }
        record.setHandlerName(currentUserName(operatorUserId));
        return toInterviewVo(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeInterviewVO> listInterviews(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        requireConfirmedArchive(employeeUuid);
        List<EmployeeInterviewPo> rows = employeeInterviewDao.selectByEmployeeUuid(employeeUuid);
        List<EmployeeInterviewVO> result = new ArrayList<>();
        if (rows != null) {
            for (EmployeeInterviewPo row : rows) {
                result.add(toInterviewVo(row));
            }
        }
        return result;
    }

    @Override
    @Transactional
    public EmployeeInterviewVO updateInterview(String employeeUuid, String interviewUuid,
                                               EmployeeInterviewSaveRequest request, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        if (request == null) {
            throw ApiException.badRequest("面谈记录不能为空");
        }
        request.rejectUnexpectedFields();
        EmployeePo employee = requireConfirmedArchiveForUpdate(employeeUuid);
        EmployeeInterviewPo existing = requireInterview(employee.getEmployeeUuid(), interviewUuid);
        if (request.getInterviewType() == null) {
            throw ApiException.badRequest("面谈类型不能为空");
        }
        if (request.getInterviewTime() == null) {
            throw ApiException.badRequest("面谈时间不能为空");
        }
        existing.setInterviewType(request.getInterviewType().getCode());
        existing.setInterviewTime(request.getInterviewTime());
        existing.setContent(cleanInterviewContent(request.getContent()));
        existing.setUpdatedAt(LocalDateTime.now());
        if (employeeInterviewDao.updateByUuid(existing) != 1) {
            throw ApiException.notFound("面谈记录不存在");
        }
        return toInterviewVo(existing);
    }

    @Override
    @Transactional
    public void deleteInterview(String employeeUuid, String interviewUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        EmployeePo employee = requireConfirmedArchiveForUpdate(employeeUuid);
        EmployeeInterviewPo existing = requireInterview(employee.getEmployeeUuid(), interviewUuid);
        if (employeeInterviewDao.deleteByUuid(existing.getInterviewUuid()) != 1) {
            throw ApiException.notFound("面谈记录不存在");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeContractVO> listContracts(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        requireConfirmedArchive(employeeUuid);
        return loadContracts(employeeUuid);
    }

    @Override
    @Transactional
    public EmployeeContractVO saveContract(String employeeUuid, EmployeeContractSaveRequest request,
                                           Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        if (request == null) {
            throw ApiException.badRequest("劳动合同不能为空");
        }
        request.rejectUnexpectedFields();
        EmployeePo employee = requireConfirmedArchiveForUpdate(employeeUuid);
        if (request.getSignType() == null) {
            throw ApiException.badRequest("签订类型不能为空");
        }
        ContractTermType termType = request.getContractTermType() == null
                ? ContractTermType.FIXED : request.getContractTermType();
        if (termType == ContractTermType.UNLIMITED && request.getEndDate() != null) {
            throw ApiException.badRequest("无固定期限合同不能填写到期日");
        }
        if (request.getProbationMonths() != null && request.getProbationMonths() < 0) {
            throw ApiException.badRequest("试用期月数不能为负数");
        }
        ContractStatus status = request.getContractStatus() == null
                ? ContractStatus.EXECUTING : request.getContractStatus();

        EmployeeContractPo record = new EmployeeContractPo();
        record.setContractUuid(UUID.randomUUID().toString());
        record.setEmployeeUuid(employee.getEmployeeUuid());
        record.setContractNo(trimToNull(request.getContractNo()));
        record.setSignType(request.getSignType().getCode());
        record.setContractTermType(termType.getCode());
        record.setStartDate(request.getStartDate());
        record.setEndDate(request.getEndDate());
        if (request.getSalary() != null) {
            record.setSalary(requireNonNegative(request.getSalary(), "合同薪资").setScale(2, RoundingMode.HALF_UP));
        }
        record.setProbationMonths(request.getProbationMonths());
        if (request.getProbationSalary() != null) {
            record.setProbationSalary(requireNonNegative(request.getProbationSalary(), "试用期薪资")
                    .setScale(2, RoundingMode.HALF_UP));
        }
        record.setCompanyName(trimToNull(request.getCompanyName()));
        record.setSocialSecurityNo(trimToNull(request.getSocialSecurityNo()));
        record.setHousingFundNo(trimToNull(request.getHousingFundNo()));
        record.setContractStatus(status.getCode());
        record.setCreatedBy(operatorUserId);
        record.setCreatedAt(LocalDateTime.now());
        if (employeeContractDao.insert(record) != 1) {
            throw ApiException.internalServerError("劳动合同保存失败");
        }
        return toContractVo(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeContractAttachmentVO> listAttachments(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        requireConfirmedArchive(employeeUuid);
        return loadAttachments(employeeUuid);
    }

    @Override
    @Transactional
    public EmployeeContractAttachmentVO saveAttachment(String employeeUuid,
                                                       EmployeeContractAttachmentSaveRequest request,
                                                       Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        if (request == null) {
            throw ApiException.badRequest("附件不能为空");
        }
        request.rejectUnexpectedFields();
        EmployeePo employee = requireConfirmedArchiveForUpdate(employeeUuid);
        if (request.getAttachmentType() == null) {
            throw ApiException.badRequest("附件类型不能为空");
        }
        String fileName = trimToNull(request.getFileName());
        if (fileName == null) {
            throw ApiException.badRequest("文件名不能为空");
        }
        if (request.getFileSize() != null && request.getFileSize() < 0) {
            throw ApiException.badRequest("文件大小不能为负数");
        }
        if (request.getSortOrder() != null && request.getSortOrder() < 0) {
            throw ApiException.badRequest("排序号不能为负数");
        }
        String fileUrl = request.getFileUrl() == null ? "" : request.getFileUrl().trim();

        EmployeeContractAttachmentPo record = new EmployeeContractAttachmentPo();
        record.setAttachmentUuid(UUID.randomUUID().toString());
        record.setEmployeeUuid(employee.getEmployeeUuid());
        record.setAttachmentType(request.getAttachmentType().getCode());
        record.setFileName(fileName);
        record.setFileUrl(fileUrl);
        record.setFileType(trimToNull(request.getFileType()));
        record.setFileSize(request.getFileSize());
        record.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        record.setCreatedBy(operatorUserId);
        record.setCreatedAt(LocalDateTime.now());
        if (employeeContractAttachmentDao.insert(record) != 1) {
            throw ApiException.internalServerError("附件保存失败");
        }
        return toAttachmentVo(record);
    }

    @Override
    @Transactional
    public EmployeeArchiveListItemVO regularize(String employeeUuid, Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        EmployeePo employee = requireConfirmedArchiveForUpdate(employeeUuid);
        if (!EmploymentStatus.PROBATION.getCode().equals(employee.getEmploymentStatus())) {
            throw ApiException.badRequest("只有试用员工可以办理转正");
        }
        LocalDate today = LocalDate.now(clock.withZone(TimeConfig.BUSINESS_ZONE));
        LocalDateTime now = LocalDateTime.now();
        if (employeeDao.regularize(employee.getEmployeeUuid(), today, now) != 1) {
            throw ApiException.badRequest("只有试用员工可以办理转正");
        }
        employee.setEmploymentStatus(EmploymentStatus.REGULAR.getCode());
        employee.setRegularizedAt(today);
        employee.setUpdatedAt(now);
        return toListItem(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeePrintPreviewVO getPrintPreview(String employeeUuid, List<String> sections,
                                                   Integer operatorUserId) {
        roleAuthorizationService.requireHrOrAdmin(operatorUserId);
        Set<PrintSection> selected = parseSections(sections);
        EmployeePrintPreviewVO vo = new EmployeePrintPreviewVO();
        if (selected == null) {
            vo.setBasicInfo(getDetail(employeeUuid, operatorUserId));
            vo.setSalaries(loadSalaries(employeeUuid));
            vo.setAccounts(listAccounts(employeeUuid, operatorUserId));
            vo.setAssignments(toAssignmentVos(employeeAssignmentRecordDao.selectByEmployeeUuid(employeeUuid)));
            vo.setInterviews(listInterviews(employeeUuid, operatorUserId));
            vo.setAttachments(loadAttachments(employeeUuid));
            return vo;
        }
        requireConfirmedArchive(employeeUuid);
        if (intersectsProfile(selected)) {
            EmployeeArchiveDetailVO detail = getDetail(employeeUuid, operatorUserId);
            retainPrintProfile(detail, selected);
            vo.setBasicInfo(detail);
        }
        vo.setSalaries(selected.contains(PrintSection.SALARY) ? loadSalaries(employeeUuid) : List.of());
        vo.setAccounts(selected.contains(PrintSection.ACCOUNT)
                ? listAccounts(employeeUuid, operatorUserId) : List.of());
        vo.setAssignments(selected.contains(PrintSection.ASSIGNMENT)
                ? toAssignmentVos(employeeAssignmentRecordDao.selectByEmployeeUuid(employeeUuid)) : List.of());
        vo.setInterviews(selected.contains(PrintSection.INTERVIEW)
                ? listInterviews(employeeUuid, operatorUserId) : List.of());
        vo.setContracts(selected.contains(PrintSection.CONTRACT) ? loadContracts(employeeUuid) : List.of());
        vo.setAttachments(selected.contains(PrintSection.ATTACHMENT)
                ? loadAttachments(employeeUuid) : List.of());
        return vo;
    }

    private EmployeeContractVO toContractVo(EmployeeContractPo row) {
        EmployeeContractVO vo = new EmployeeContractVO();
        vo.setContractUuid(row.getContractUuid());
        vo.setContractNo(row.getContractNo());
        ContractSignType signType = ContractSignType.fromCode(row.getSignType());
        vo.setSignType(signType != null ? signType.getCode() : row.getSignType());
        vo.setSignTypeLabel(signType != null ? signType.getLabel() : row.getSignType());
        ContractTermType termType = ContractTermType.fromCode(row.getContractTermType());
        vo.setContractTermType(termType != null ? termType.getCode() : row.getContractTermType());
        vo.setContractTermTypeLabel(termType != null ? termType.getLabel() : row.getContractTermType());
        vo.setStartDate(row.getStartDate());
        vo.setEndDate(row.getEndDate());
        vo.setSalary(row.getSalary());
        vo.setProbationMonths(row.getProbationMonths());
        vo.setProbationSalary(row.getProbationSalary());
        vo.setCompanyName(row.getCompanyName());
        vo.setSocialSecurityNo(row.getSocialSecurityNo());
        vo.setHousingFundNo(row.getHousingFundNo());
        ContractStatus status = ContractStatus.fromCode(row.getContractStatus());
        vo.setContractStatus(status != null ? status.getCode() : row.getContractStatus());
        vo.setContractStatusLabel(status != null ? status.getLabel() : row.getContractStatus());
        vo.setCreatedAt(row.getCreatedAt());
        return vo;
    }

    private EmployeeContractAttachmentVO toAttachmentVo(EmployeeContractAttachmentPo row) {
        EmployeeContractAttachmentVO vo = new EmployeeContractAttachmentVO();
        vo.setAttachmentUuid(row.getAttachmentUuid());
        AttachmentType type = AttachmentType.fromCode(row.getAttachmentType());
        vo.setAttachmentType(type != null ? type.getCode() : row.getAttachmentType());
        vo.setAttachmentTypeLabel(type != null ? type.getLabel() : row.getAttachmentType());
        vo.setFileName(row.getFileName());
        vo.setFileUrl(row.getFileUrl());
        vo.setFileType(row.getFileType());
        vo.setFileSize(row.getFileSize());
        vo.setSortOrder(row.getSortOrder());
        vo.setCreatedAt(row.getCreatedAt());
        return vo;
    }

    private EmployeeInterviewVO toInterviewVo(EmployeeInterviewPo row) {
        EmployeeInterviewVO vo = new EmployeeInterviewVO();
        vo.setInterviewUuid(row.getInterviewUuid());
        vo.setEmployeeUuid(row.getEmployeeUuid());
        InterviewType type = InterviewType.fromCode(row.getInterviewType());
        vo.setInterviewType(type);
        vo.setInterviewTypeLabel(type != null ? type.getLabel() : row.getInterviewType());
        vo.setInterviewTime(row.getInterviewTime());
        vo.setContent(row.getContent());
        vo.setHandlerName(row.getHandlerName());
        vo.setCreatedAt(row.getCreatedAt());
        return vo;
    }

    private String currentUserName(Integer userId) {
        if (userId == null) {
            return null;
        }
        UserPo user = userDao.selectUserById(userId);
        return user == null ? null : user.getUsername();
    }

    private EmployeePo requireConfirmedArchiveForUpdate(String employeeUuid) {
        validateEmployeeUuid(employeeUuid);
        EmployeePo employee = employeeDao.selectByEmployeeUuidForUpdate(employeeUuid);
        if (employee == null || employee.getHrConfirmedAt() == null) {
            throw ApiException.notFound("员工档案不存在");
        }
        return employee;
    }

    private EmployeePo requireConfirmedArchive(String employeeUuid) {
        validateEmployeeUuid(employeeUuid);
        EmployeePo employee = employeeDao.selectByEmployeeUuid(employeeUuid);
        if (employee == null || employee.getHrConfirmedAt() == null) {
            throw ApiException.notFound("员工档案不存在");
        }
        return employee;
    }

    private EmployeeArchiveHeaderVO toHeader(EmployeePo employee) {
        LocalDate today = LocalDate.now(clock.withZone(TimeConfig.BUSINESS_ZONE));
        EmployeeArchiveHeaderVO header = new EmployeeArchiveHeaderVO();
        header.setEmployeeUuid(employee.getEmployeeUuid());
        header.setFullName(employee.getFullName());
        header.setEmployeeNo(employee.getEmployeeNo());
        header.setEmploymentStatus(EmploymentStatus.fromCode(employee.getEmploymentStatus()));
        header.setGender(employee.getGender());
        header.setAge(ageInYears(employee.getBirthDate(), today));
        header.setPhone(employee.getPhone());
        header.setCompanyEmail(employee.getCompanyEmail());
        header.setDepartment(employee.getDepartment());
        header.setPosition(employee.getPosition());
        header.setTenureMonths(tenureMonths(employee.getHiredAt(), today));
        header.setProbationRemainingDays(probationRemainingDays(
                employee.getEmploymentStatus(), employee.getProbationEndDate(), today));
        header.setHiredAt(employee.getHiredAt());
        header.setHrSystemAccount(hrSystemAccount(employee.getUserId()));
        header.setEmploymentType(EmploymentType.fromCode(employee.getEmploymentType()));
        return header;
    }

    private String hrSystemAccount(Integer userId) {
        if (userId == null) {
            return null;
        }
        UserPo user = userDao.selectUserById(userId);
        return user == null ? null : user.getUsername();
    }

    static Integer ageInYears(LocalDate birthDate, LocalDate today) {
        if (birthDate == null || today == null || birthDate.isAfter(today)) {
            return null;
        }
        return Period.between(birthDate, today).getYears();
    }

    static Integer tenureMonths(LocalDate hiredAt, LocalDate today) {
        if (hiredAt == null || today == null || hiredAt.isAfter(today)) {
            return null;
        }
        Period period = Period.between(hiredAt, today);
        return period.getYears() * 12 + period.getMonths();
    }

    static Integer probationRemainingDays(String employmentStatus, LocalDate probationEndDate,
                                          LocalDate today) {
        if (!EmploymentStatus.PROBATION.getCode().equals(employmentStatus) || probationEndDate == null
                || today == null) {
            return null;
        }
        long remaining = ChronoUnit.DAYS.between(today, probationEndDate);
        return remaining < 0 ? 0 : (int) remaining;
    }

    private static void applyAllowedUpdates(EmployeePo employee, EmployeeArchiveUpdateRequest request) {
        if (request.getDepartment() != null) {
            employee.setDepartment(trimToNull(request.getDepartment()));
        }
        if (request.getCompanyEmail() != null) {
            employee.setCompanyEmail(trimToNull(request.getCompanyEmail()));
        }
        if (request.getProbationEndDate() != null) {
            employee.setProbationEndDate(request.getProbationEndDate());
        }
        if (request.getContractSalary() != null) {
            employee.setContractSalary(requireNonNegative(request.getContractSalary(), "合同薪资"));
        }
        if (request.getProbationSalary() != null) {
            employee.setProbationSalary(requireNonNegative(request.getProbationSalary(), "试用期薪资"));
        }
        if (request.getHiredAt() != null) {
            if (employee.getHiredAt() != null) {
                throw ApiException.conflict("入职时间已存在，不可修改");
            }
            employee.setHiredAt(request.getHiredAt());
        }
    }

    private static EmployeeArchivePageRequest normalizePageRequest(EmployeeArchivePageRequest request) {
        EmployeeArchivePageRequest condition = request == null ? new EmployeeArchivePageRequest() : request;
        if (condition.getPage() == null) {
            condition.setPage(1);
        }
        if (condition.getPageSize() == null) {
            condition.setPageSize(20);
        }
        condition.setFullName(trimToNull(condition.getFullName()));
        condition.setPhone(trimToNull(condition.getPhone()));
        condition.setCustomerName(trimToNull(condition.getCustomerName()));
        condition.setPosition(trimToNull(condition.getPosition()));
        String status = trimToNull(condition.getEmploymentStatus());
        if (status != null && EmploymentStatus.fromCode(status) == null) {
            throw ApiException.badRequest("在职状态不正确");
        }
        condition.setEmploymentStatus(status);
        return condition;
    }

    private static EmployeeArchiveListItemVO toListItem(EmployeePo employee) {
        EmployeeArchiveListItemVO item = new EmployeeArchiveListItemVO();
        item.setEmployeeUuid(employee.getEmployeeUuid());
        item.setFullName(employee.getFullName());
        item.setEmployeeNo(employee.getEmployeeNo());
        item.setGender(employee.getGender());
        item.setPhone(employee.getPhone());
        item.setEmploymentStatus(EmploymentStatus.fromCode(employee.getEmploymentStatus()));
        item.setHiredAt(employee.getHiredAt());
        item.setRegularizedAt(employee.getRegularizedAt());
        item.setPosition(employee.getPosition());
        item.setCustomerName(employee.getCustomerName());
        item.setContractSalary(employee.getContractSalary());
        item.setProbationSalary(employee.getProbationSalary());
        item.setEmploymentType(EmploymentType.fromCode(employee.getEmploymentType()));
        return item;
    }

    private static EmployeeSalaryRecordPo toSalaryRecord(String employeeUuid, EmployeeSalarySaveRequest request,
                                                         Integer operatorUserId) {
        if (request.getSalaryMonth() == null) {
            throw ApiException.badRequest("工资所属月不能为空");
        }
        BigDecimal baseSalary = money(request.getBaseSalary(), "基本工资");
        BigDecimal positionAllowance = money(request.getPositionAllowance(), "岗位津贴");
        BigDecimal overtimePay = money(request.getOvertimePay(), "加班费");
        BigDecimal bonus = money(request.getBonus(), "奖金");
        BigDecimal subsidy = money(request.getSubsidy(), "补贴");
        BigDecimal otherPay = money(request.getOtherPay(), "其他应发");
        BigDecimal socialInsurance = money(request.getSocialInsurance(), "社保");
        BigDecimal housingFund = money(request.getHousingFund(), "公积金");
        BigDecimal taxAmount = money(request.getTaxAmount(), "个税");
        BigDecimal grossPay = baseSalary.add(positionAllowance).add(overtimePay).add(bonus).add(subsidy).add(otherPay);
        BigDecimal netPay = grossPay.subtract(socialInsurance).subtract(housingFund).subtract(taxAmount);
        if (netPay.compareTo(BigDecimal.ZERO) < 0) {
            throw ApiException.badRequest("实发不能为负数");
        }
        LocalDateTime now = LocalDateTime.now();
        EmployeeSalaryRecordPo record = new EmployeeSalaryRecordPo();
        record.setSalaryUuid(UUID.randomUUID().toString());
        record.setEmployeeUuid(employeeUuid);
        record.setSalaryMonth(request.getSalaryMonth().withDayOfMonth(1));
        record.setDepartment(trimToNull(request.getDepartment()));
        record.setScheduledDays(nonNegativeDay(request.getScheduledDays(), "应出勤天数"));
        record.setActualDays(nonNegativeDay(request.getActualDays(), "实出勤天数"));
        record.setLeaveDays(nonNegativeDay(request.getLeaveDays(), "请假天数"));
        record.setBaseSalary(baseSalary);
        record.setPositionAllowance(positionAllowance);
        record.setOvertimePay(overtimePay);
        record.setBonus(bonus);
        record.setSubsidy(subsidy);
        record.setOtherPay(otherPay);
        record.setSocialInsurance(socialInsurance);
        record.setHousingFund(housingFund);
        record.setTaxAmount(taxAmount);
        record.setTaxRate(nullableRate(request.getTaxRate()));
        record.setGrossPay(grossPay);
        record.setNetPay(netPay);
        record.setRemark(trimToNull(request.getRemark()));
        record.setCreatedBy(operatorUserId);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        return record;
    }

    private static EmployeeSalaryRecordVO toSalaryVo(EmployeeSalaryRecordPo source) {
        EmployeeSalaryRecordVO result = new EmployeeSalaryRecordVO();
        result.setSalaryUuid(source.getSalaryUuid());
        result.setEmployeeUuid(source.getEmployeeUuid());
        result.setSalaryMonth(source.getSalaryMonth());
        result.setDepartment(source.getDepartment());
        result.setScheduledDays(source.getScheduledDays());
        result.setActualDays(source.getActualDays());
        result.setLeaveDays(source.getLeaveDays());
        result.setBaseSalary(source.getBaseSalary());
        result.setPositionAllowance(source.getPositionAllowance());
        result.setOvertimePay(source.getOvertimePay());
        result.setBonus(source.getBonus());
        result.setSubsidy(source.getSubsidy());
        result.setOtherPay(source.getOtherPay());
        result.setSocialInsurance(source.getSocialInsurance());
        result.setHousingFund(source.getHousingFund());
        result.setTaxAmount(source.getTaxAmount());
        result.setTaxRate(source.getTaxRate());
        result.setGrossPay(source.getGrossPay());
        result.setNetPay(source.getNetPay());
        result.setRemark(source.getRemark());
        result.setCreatedAt(source.getCreatedAt());
        return result;
    }

    private static Integer nonNegativeDay(Integer days, String fieldName) {
        if (days == null) {
            return null;
        }
        if (days < 0) {
            throw ApiException.badRequest(fieldName + "不能为负数");
        }
        return days;
    }

    private static BigDecimal money(BigDecimal value, String fieldName) {
        BigDecimal amount = value == null ? BigDecimal.ZERO : value;
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw ApiException.badRequest(fieldName + "不能为负数");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nullableRate(BigDecimal taxRate) {
        if (taxRate == null) {
            return null;
        }
        if (taxRate.compareTo(BigDecimal.ZERO) < 0) {
            throw ApiException.badRequest("税率不能为负数");
        }
        return taxRate.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? zeroMoney() : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal zeroMoney() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private static void validateAssignmentPayload(EmployeeAssignmentSaveRequest request) {
        if (request.getAssignmentType() == AssignmentType.ENTER) {
            if (trimToNull(request.getCompanyName()) == null) {
                throw ApiException.badRequest("进入客户必须填写客户名称");
            }
            if (request.getUtilizationRate() != null) {
                BigDecimal rate = request.getUtilizationRate();
                if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(new BigDecimal("100")) > 0) {
                    throw ApiException.badRequest("稼动率必须在 0 到 100 之间");
                }
            }
            return;
        }
        if (trimToNull(request.getCompanyName()) != null) {
            throw ApiException.badRequest("回公司不能填写客户名称");
        }
        if (request.getUtilizationRate() != null) {
            throw ApiException.badRequest("回公司不能填写稼动率");
        }
    }

    private static EmployeeAssignmentRecordPo lastEvent(List<EmployeeAssignmentRecordPo> existing) {
        if (existing == null || existing.isEmpty()) {
            return null;
        }
        return existing.get(existing.size() - 1);
    }

    private static EmployeeAssignmentRecordPo toAssignmentPo(String employeeUuid,
                                                             EmployeeAssignmentSaveRequest request,
                                                             Integer operatorUserId) {
        LocalDateTime now = LocalDateTime.now();
        EmployeeAssignmentRecordPo record = new EmployeeAssignmentRecordPo();
        record.setAssignmentUuid(UUID.randomUUID().toString());
        record.setEmployeeUuid(employeeUuid);
        record.setAssignmentType(request.getAssignmentType().getCode());
        record.setEventDate(request.getEventDate());
        record.setCompanyName(trimToNull(request.getCompanyName()));
        record.setUtilizationRate(request.getUtilizationRate() == null
                ? null : request.getUtilizationRate().setScale(2, RoundingMode.HALF_UP));
        record.setOperatorUserId(operatorUserId);
        record.setRemark(trimToNull(request.getRemark()));
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        return record;
    }

    private void applyAssignmentToEmployee(EmployeePo employee, EmployeeAssignmentSaveRequest request) {
        employee.setUpdatedAt(LocalDateTime.now());
        if (request.getAssignmentType() == AssignmentType.ENTER) {
            employee.setWorkLocation(WorkLocation.DISPATCHED.getCode());
            employee.setCustomerName(trimToNull(request.getCompanyName()));
            return;
        }
        employee.setWorkLocation(WorkLocation.HEADQUARTERS.getCode());
        employee.setCustomerName(null);
    }

    private List<EmployeeAssignmentRecordVO> toAssignmentVos(List<EmployeeAssignmentRecordPo> source) {
        List<EmployeeAssignmentRecordVO> result = new ArrayList<>();
        if (source == null || source.isEmpty()) {
            return result;
        }
        LocalDate today = LocalDate.now(clock.withZone(TimeConfig.BUSINESS_ZONE));
        for (int i = 0; i < source.size(); i++) {
            EmployeeAssignmentRecordPo item = source.get(i);
            EmployeeAssignmentRecordVO vo = new EmployeeAssignmentRecordVO();
            vo.setAssignmentUuid(item.getAssignmentUuid());
            vo.setAssignmentType(AssignmentType.fromCode(item.getAssignmentType()));
            vo.setEventDate(item.getEventDate());
            vo.setCompanyName(item.getCompanyName());
            vo.setUtilizationRate(item.getUtilizationRate());
            vo.setOperatorName(item.getOperatorName());
            vo.setRemark(item.getRemark());
            if (AssignmentType.ENTER.getCode().equals(item.getAssignmentType()) && item.getEventDate() != null) {
                vo.setStartDate(item.getEventDate());
                LocalDate endDate = nextReturnDate(source, i);
                vo.setEndDate(endDate);
                boolean inProgress = endDate == null;
                vo.setInProgress(inProgress);
                vo.setDurationText(inProgress ? "进行中" : formatDuration(item.getEventDate(), endDate));
                LocalDate dayEnd = inProgress ? today : endDate;
                vo.setUtilizationDays(utilizationDays(item.getEventDate(), dayEnd));
            }
            result.add(vo);
        }
        return result;
    }

    private static LocalDate nextReturnDate(List<EmployeeAssignmentRecordPo> source, int enterIndex) {
        for (int i = enterIndex + 1; i < source.size(); i++) {
            if (AssignmentType.RETURN.getCode().equals(source.get(i).getAssignmentType())) {
                return source.get(i).getEventDate();
            }
        }
        return null;
    }

    private EmployeeAssignmentSummaryVO summarizeAssignments(List<EmployeeAssignmentRecordVO> records) {
        EmployeeAssignmentSummaryVO summary = new EmployeeAssignmentSummaryVO();
        if (records == null || records.isEmpty()) {
            summary.setProjectCount(0);
            summary.setInProgressCount(0);
            summary.setAccumulatedDurationText("0天");
            return summary;
        }
        LocalDate today = LocalDate.now(clock.withZone(TimeConfig.BUSINESS_ZONE));
        long projectCount = 0;
        long inProgressCount = 0;
        Period accumulated = Period.ZERO;
        BigDecimal weightedRate = BigDecimal.ZERO;
        long ratedDays = 0;
        for (EmployeeAssignmentRecordVO record : records) {
            if (record.getAssignmentType() != AssignmentType.ENTER || record.getStartDate() == null) {
                continue;
            }
            projectCount++;
            LocalDate end = record.getEndDate();
            if (record.isInProgress()) {
                inProgressCount++;
                end = today;
            }
            if (end != null) {
                accumulated = accumulated.plus(Period.between(record.getStartDate(), end));
            }
            if (record.getUtilizationRate() != null) {
                int days = record.getUtilizationDays() == null ? 0 : record.getUtilizationDays();
                weightedRate = weightedRate.add(record.getUtilizationRate().multiply(BigDecimal.valueOf(days)));
                ratedDays += days;
            }
        }
        summary.setProjectCount(projectCount);
        summary.setInProgressCount(inProgressCount);
        summary.setAccumulatedDurationText(formatDuration(accumulated.normalized()));
        summary.setAccumulatedUtilizationRate(ratedDays == 0
                ? null
                : weightedRate.divide(BigDecimal.valueOf(ratedDays), 2, RoundingMode.HALF_UP));
        return summary;
    }

    static String formatDuration(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return "进行中";
        }
        return formatDuration(Period.between(start, end).normalized());
    }

    static String formatDuration(Period period) {
        if (period == null || period.isZero() || period.isNegative()) {
            return "0天";
        }
        StringBuilder text = new StringBuilder();
        if (period.getYears() > 0) {
            text.append(period.getYears()).append("年");
        }
        if (period.getMonths() > 0) {
            text.append(period.getMonths()).append("个月");
        }
        if (period.getDays() > 0) {
            text.append(period.getDays()).append("天");
        }
        return text.length() == 0 ? "0天" : text.toString();
    }

    private EmployeeSystemAccountVO hrSystemAccountRow(EmployeePo employee) {
        EmployeeSystemAccountVO row = new EmployeeSystemAccountVO();
        row.setSystemName("人力资源系统");
        row.setAccountName(hrSystemAccount(employee.getUserId()));
        row.setReadonly(true);
        return row;
    }

    private static EmployeeSystemAccountVO toAccountVo(EmployeeSystemAccountPo source, boolean readonly) {
        EmployeeSystemAccountVO result = new EmployeeSystemAccountVO();
        result.setAccountUuid(source.getAccountUuid());
        result.setSystemName(source.getSystemName());
        result.setAccountName(source.getAccountName());
        result.setPassword(source.getPassword());
        result.setOpenedAt(source.getOpenedAt());
        result.setReadonly(readonly);
        return result;
    }

    private static BigDecimal requireNonNegative(BigDecimal amount, String fieldName) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw ApiException.badRequest(fieldName + "不能为负数");
        }
        return amount;
    }

    private static Long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private static void validateEmployeeUuid(String employeeUuid) {
        if (employeeUuid == null || employeeUuid.isBlank()) {
            throw ApiException.badRequest("员工档案 UUID 不能为空");
        }
        try {
            UUID.fromString(employeeUuid);
        } catch (IllegalArgumentException exception) {
            throw ApiException.badRequest("员工档案 UUID 格式不正确");
        }
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String cleanInterviewContent(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        return Jsoup.clean(content, Safelist.basic());
    }

    private EmployeeInterviewPo requireInterview(String employeeUuid, String interviewUuid) {
        if (interviewUuid == null || interviewUuid.isBlank()) {
            throw ApiException.badRequest("面谈记录 UUID 不能为空");
        }
        try {
            UUID.fromString(interviewUuid);
        } catch (IllegalArgumentException exception) {
            throw ApiException.badRequest("面谈记录 UUID 格式不正确");
        }
        EmployeeInterviewPo existing = employeeInterviewDao.selectByUuid(interviewUuid);
        if (existing == null || !employeeUuid.equals(existing.getEmployeeUuid())) {
            throw ApiException.notFound("面谈记录不存在");
        }
        return existing;
    }

    private static Integer utilizationDays(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(start, end);
        if (days <= 0 || days > Integer.MAX_VALUE) {
            return 0;
        }
        return (int) days;
    }

    private List<EmployeeSalaryRecordVO> loadSalaries(String employeeUuid) {
        List<EmployeeSalaryRecordVO> salaries = new ArrayList<>();
        List<EmployeeSalaryRecordPo> salaryRows = employeeSalaryRecordDao.selectByEmployeeUuid(employeeUuid);
        if (salaryRows != null) {
            for (EmployeeSalaryRecordPo row : salaryRows) {
                salaries.add(toSalaryVo(row));
            }
        }
        return salaries;
    }

    private List<EmployeeContractVO> loadContracts(String employeeUuid) {
        List<EmployeeContractVO> result = new ArrayList<>();
        List<EmployeeContractPo> rows = employeeContractDao.selectByEmployeeUuid(employeeUuid);
        if (rows != null) {
            for (EmployeeContractPo row : rows) {
                result.add(toContractVo(row));
            }
        }
        return result;
    }

    private List<EmployeeContractAttachmentVO> loadAttachments(String employeeUuid) {
        List<EmployeeContractAttachmentVO> attachments = new ArrayList<>();
        List<EmployeeContractAttachmentPo> rows = employeeContractAttachmentDao.selectByEmployeeUuid(employeeUuid);
        if (rows != null) {
            for (EmployeeContractAttachmentPo row : rows) {
                attachments.add(toAttachmentVo(row));
            }
        }
        return attachments;
    }

    private static final Set<PrintSection> PROFILE_SECTIONS = EnumSet.of(
            PrintSection.BASIC, PrintSection.EXPERIENCE, PrintSection.FAMILY, PrintSection.EMERGENCY);

    private static boolean intersectsProfile(Set<PrintSection> selected) {
        for (PrintSection section : PROFILE_SECTIONS) {
            if (selected.contains(section)) {
                return true;
            }
        }
        return false;
    }

    private static Set<PrintSection> parseSections(List<String> sections) {
        if (sections == null || sections.isEmpty()) {
            return null;
        }
        Set<PrintSection> selected = EnumSet.noneOf(PrintSection.class);
        boolean any = false;
        for (String raw : sections) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            for (String token : raw.split(",")) {
                String code = token.trim();
                if (code.isEmpty()) {
                    continue;
                }
                any = true;
                PrintSection section = PrintSection.fromCode(code);
                if (section == null) {
                    throw ApiException.badRequest("未知的打印模块");
                }
                selected.add(section);
            }
        }
        return any ? selected : null;
    }

    private static void retainPrintProfile(EmployeeArchiveDetailVO detail, Set<PrintSection> selected) {
        if (!selected.contains(PrintSection.BASIC)) {
            detail.setHeader(null);
            detail.setEmployeeUuid(null);
            detail.setEmail(null);
            detail.setIdCard(null);
            detail.setBirthDate(null);
            detail.setMaritalStatus(null);
            detail.setPoliticalStatus(null);
            detail.setNationality(null);
            detail.setEthnicity(null);
            detail.setNativePlace(null);
            detail.setHukouLocation(null);
            detail.setCurrentAddress(null);
            detail.setPostalCode(null);
            detail.setHealthStatus(null);
            detail.setHighestEducation(null);
            detail.setMajor(null);
            detail.setProfessionalTitle(null);
            detail.setForeignLanguage(null);
            detail.setHobbies(null);
            detail.setWechatAccount(null);
            detail.setPhotoPath(null);
            detail.setPhotoUploaded(false);
        }
        if (!selected.contains(PrintSection.EXPERIENCE)) {
            detail.setEducations(List.of());
            detail.setWorkHistories(List.of());
            detail.setTrainings(List.of());
        }
        if (!selected.contains(PrintSection.FAMILY)) {
            detail.setFamilyMembers(List.of());
        }
        if (!selected.contains(PrintSection.EMERGENCY)) {
            detail.setEmergencyContacts(List.of());
        }
    }
}
