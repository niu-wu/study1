package com.example.study11.entity.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 入职登记 10 步进度。由后端按当前表单数据计算。 */
@Data
public class OnboardingProgressVO {

    private int totalSteps;

    private int completedSteps;

    private int percent;

    private List<OnboardingStepProgressVO> steps = new ArrayList<>();
}
