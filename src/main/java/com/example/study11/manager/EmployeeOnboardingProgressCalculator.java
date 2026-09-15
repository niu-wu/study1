package com.example.study11.manager;

import com.example.study11.entity.dto.EmployeeEducationItemDTO;
import com.example.study11.entity.dto.EmployeeEmergencyContactItemDTO;
import com.example.study11.entity.dto.EmployeeFamilyItemDTO;
import com.example.study11.entity.dto.EmployeeTrainingItemDTO;
import com.example.study11.entity.dto.EmployeeWorkHistoryItemDTO;
import com.example.study11.entity.enums.OnboardingStep;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.OnboardingProgressVO;
import com.example.study11.entity.vo.OnboardingStepProgressVO;
import com.example.study11.exception.ApiException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** 按登记表当前内容计算 10 步进度，并校验提交必填步。 */
@Component
public class EmployeeOnboardingProgressCalculator {

    public OnboardingProgressVO calculate(EmployeeOnboardingFormVO form) {
        EmployeeOnboardingFormVO source = form == null ? new EmployeeOnboardingFormVO() : form;
        OnboardingProgressVO progress = new OnboardingProgressVO();
        List<OnboardingStepProgressVO> steps = new ArrayList<>();
        int completed = 0;
        for (OnboardingStep step : OnboardingStep.values()) {
            OnboardingStepProgressVO item = new OnboardingStepProgressVO();
            item.setCode(step.getCode());
            item.setName(step.getDisplayName());
            item.setRequired(step.isRequired());
            boolean done = isCompleted(step, source);
            item.setCompleted(done);
            if (done) {
                completed++;
            }
            steps.add(item);
        }
        progress.setTotalSteps(OnboardingStep.values().length);
        progress.setCompletedSteps(completed);
        progress.setPercent(completed * 100 / OnboardingStep.values().length);
        progress.setSteps(steps);
        return progress;
    }

    public void validateRequiredSteps(EmployeeOnboardingFormVO form) {
        validateRequiredSteps(form, Set.of());
    }

    public void validateRequiredSteps(EmployeeOnboardingFormVO form, Set<OnboardingStep> skipped) {
        Set<OnboardingStep> ignored = skipped == null || skipped.isEmpty()
                ? Set.of()
                : Collections.unmodifiableSet(EnumSet.copyOf(skipped));
        OnboardingProgressVO progress = calculate(form);
        List<String> missing = new ArrayList<>();
        for (OnboardingStepProgressVO step : progress.getSteps()) {
            if (!step.isRequired() || step.isCompleted()) {
                continue;
            }
            OnboardingStep current = OnboardingStep.fromCode(step.getCode());
            if (current != null && ignored.contains(current)) {
                continue;
            }
            missing.add(step.getName());
        }
        if (!missing.isEmpty()) {
            throw ApiException.badRequest("请先完成：" + String.join("、", missing));
        }
    }

    private static boolean isCompleted(OnboardingStep step, EmployeeOnboardingFormVO form) {
        return switch (step) {
            case BASIC_INFO -> hasText(form.getFullName())
                    && hasText(form.getGender())
                    && form.getBirthDate() != null
                    && hasText(form.getPhone())
                    && hasText(form.getIdCard())
                    && hasText(form.getCurrentAddress());
            case ACCOUNT_BIND -> hasText(form.getWechatAccount()) || hasText(form.getWechatOpenid());
            case ID_DOCUMENT, EDU_CERTIFICATE -> false;
            case FAMILY -> hasCompletedFamily(form.getFamilyMembers());
            case EMERGENCY -> hasCompletedEmergency(form.getEmergencyContacts());
            case PHOTO -> hasText(form.getPhotoPath());
            case EDUCATION -> hasCompletedEducation(form.getEducations());
            case TRAINING -> hasCompletedTraining(form.getTrainings());
            case WORK_HISTORY -> hasCompletedWork(form.getWorkHistories());
        };
    }

    private static boolean hasCompletedFamily(List<EmployeeFamilyItemDTO> items) {
        if (items == null) {
            return false;
        }
        for (EmployeeFamilyItemDTO item : items) {
            if (item != null && hasText(item.getFullName()) && hasText(item.getRelationship())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasCompletedEmergency(List<EmployeeEmergencyContactItemDTO> items) {
        if (items == null) {
            return false;
        }
        for (EmployeeEmergencyContactItemDTO item : items) {
            if (item != null && hasText(item.getFullName()) && hasText(item.getPhone())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasCompletedEducation(List<EmployeeEducationItemDTO> items) {
        if (items == null) {
            return false;
        }
        for (EmployeeEducationItemDTO item : items) {
            if (item != null && hasText(item.getSchoolName()) && hasText(item.getEducationLevel())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasCompletedTraining(List<EmployeeTrainingItemDTO> items) {
        if (items == null) {
            return false;
        }
        for (EmployeeTrainingItemDTO item : items) {
            if (item != null && hasText(item.getInstitution()) && hasText(item.getCourseContent())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasCompletedWork(List<EmployeeWorkHistoryItemDTO> items) {
        if (items == null) {
            return false;
        }
        for (EmployeeWorkHistoryItemDTO item : items) {
            if (item != null && hasText(item.getCompanyName()) && hasText(item.getPosition())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
