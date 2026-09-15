package com.example.study11.entity.enums;

/** 入职登记 10 步进度。required 表示提交时是否必须完成。 */
public enum OnboardingStep {

    BASIC_INFO("BASIC_INFO", "基本信息", true),
    ACCOUNT_BIND("ACCOUNT_BIND", "账号绑定", true),
    ID_DOCUMENT("ID_DOCUMENT", "上传证件", false),
    EDU_CERTIFICATE("EDU_CERTIFICATE", "上传学历", false),
    FAMILY("FAMILY", "家庭情况", true),
    EMERGENCY("EMERGENCY", "紧急联系人", true),
    PHOTO("PHOTO", "一寸照片", true),
    EDUCATION("EDUCATION", "教育经历", true),
    TRAINING("TRAINING", "培训经历", false),
    WORK_HISTORY("WORK_HISTORY", "工作经历", false);

    private final String code;

    private final String displayName;

    private final boolean required;

    OnboardingStep(String code, String displayName, boolean required) {
        this.code = code;
        this.displayName = displayName;
        this.required = required;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRequired() {
        return required;
    }

    public static OnboardingStep fromCode(String code) {
        for (OnboardingStep step : values()) {
            if (step.code.equals(code)) {
                return step;
            }
        }
        return null;
    }
}
