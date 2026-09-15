package com.example.study11.manager;

import com.example.study11.entity.dto.EmployeeEducationItemDTO;
import com.example.study11.entity.dto.EmployeeEmergencyContactItemDTO;
import com.example.study11.entity.dto.EmployeeFamilyItemDTO;
import com.example.study11.entity.enums.OnboardingStep;
import com.example.study11.entity.vo.EmployeeOnboardingFormVO;
import com.example.study11.entity.vo.OnboardingProgressVO;
import com.example.study11.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeOnboardingProgressCalculatorTest {

    private final EmployeeOnboardingProgressCalculator calculator = new EmployeeOnboardingProgressCalculator();

    @Test
    void emptyFormCompletesNoRequiredSteps() {
        OnboardingProgressVO progress = calculator.calculate(new EmployeeOnboardingFormVO());

        assertEquals(10, progress.getTotalSteps());
        assertEquals(0, progress.getCompletedSteps());
        assertEquals(0, progress.getPercent());
        assertTrue(progress.getSteps().stream()
                .filter(step -> !"ID_DOCUMENT".equals(step.getCode()) && !"EDU_CERTIFICATE".equals(step.getCode()))
                .anyMatch(step -> step.isRequired() && !step.isCompleted()));
    }

    @Test
    void requiredStepsPassWhenCoreFieldsArePresent() {
        EmployeeOnboardingFormVO form = completeForm();

        OnboardingProgressVO progress = calculator.calculate(form);

        assertTrue(completed(progress, OnboardingStep.BASIC_INFO));
        assertTrue(completed(progress, OnboardingStep.ACCOUNT_BIND));
        assertTrue(completed(progress, OnboardingStep.FAMILY));
        assertTrue(completed(progress, OnboardingStep.EMERGENCY));
        assertTrue(completed(progress, OnboardingStep.PHOTO));
        assertTrue(completed(progress, OnboardingStep.EDUCATION));
        assertFalse(completed(progress, OnboardingStep.ID_DOCUMENT));
        assertFalse(completed(progress, OnboardingStep.EDU_CERTIFICATE));
        calculator.validateRequiredSteps(form);
    }

    @Test
    void validateCanSkipPhotoForPartTimeCreate() {
        EmployeeOnboardingFormVO form = completeForm();
        form.setPhotoPath(null);

        ApiException exception = assertThrows(ApiException.class, () -> calculator.validateRequiredSteps(form));
        assertTrue(exception.getMessage().contains("一寸照片"));

        calculator.validateRequiredSteps(form, Set.of(OnboardingStep.PHOTO));
    }

    private static boolean completed(OnboardingProgressVO progress, OnboardingStep step) {
        return progress.getSteps().stream()
                .filter(item -> step.getCode().equals(item.getCode()))
                .findFirst()
                .orElseThrow()
                .isCompleted();
    }

    private static EmployeeOnboardingFormVO completeForm() {
        EmployeeOnboardingFormVO form = new EmployeeOnboardingFormVO();
        form.setFullName("张三");
        form.setGender("男");
        form.setBirthDate(LocalDate.of(1994, 5, 20));
        form.setPhone("13800138000");
        form.setIdCard("110101199405201234");
        form.setCurrentAddress("海城区");
        form.setWechatAccount("wx_zhangsan");
        form.setPhotoPath("550e8400-e29b-41d4-a716-446655440000.jpg");
        EmployeeEducationItemDTO education = new EmployeeEducationItemDTO();
        education.setSchoolName("北海大学");
        education.setEducationLevel("本科");
        form.setEducations(List.of(education));
        EmployeeFamilyItemDTO family = new EmployeeFamilyItemDTO();
        family.setFullName("张父");
        family.setRelationship("父亲");
        form.setFamilyMembers(List.of(family));
        EmployeeEmergencyContactItemDTO contact = new EmployeeEmergencyContactItemDTO();
        contact.setFullName("李紧急");
        contact.setPhone("13900000000");
        form.setEmergencyContacts(List.of(contact));
        return form;
    }
}
