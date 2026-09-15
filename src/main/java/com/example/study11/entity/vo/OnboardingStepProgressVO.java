package com.example.study11.entity.vo;

import lombok.Data;

/** 入职登记单步进度。 */
@Data
public class OnboardingStepProgressVO {

    private String code;

    private String name;

    private boolean required;

    private boolean completed;
}
