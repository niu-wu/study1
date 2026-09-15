package com.example.study11.service;

import com.example.study11.entity.dto.EmployeeOnboardingSaveRequest;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.EmployeePhotoFileVO;
import org.springframework.web.multipart.MultipartFile;

/** 员工入职登记表业务。 */
public interface EmployeeOnboardingService {

    /** 查询当前用户登记表；尚无档案时按已入职招聘记录预填并创建草稿。 */
    EmployeeOnboardingFormVO getMyForm(Integer userId);

    /** 保存草稿。已提交的登记表不能再改。 */
    EmployeeOnboardingFormVO saveDraft(Integer userId, EmployeeOnboardingSaveRequest request);

    /** 保存当前表单并提交锁定。 */
    EmployeeOnboardingFormVO submit(Integer userId, EmployeeOnboardingSaveRequest request);

    /** 上传一寸照。已提交的登记表不能再改。 */
    EmployeeOnboardingFormVO uploadPhoto(Integer userId, MultipartFile file);

    /** 下载当前用户一寸照。 */
    EmployeePhotoFileVO loadMyPhoto(Integer userId);
}
