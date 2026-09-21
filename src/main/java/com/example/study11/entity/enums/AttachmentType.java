package com.example.study11.entity.enums;

/** 合同/证件附件类型。 */
public enum AttachmentType {

    RESUME("RESUME", "个人简历"),
    ONBOARDING_CONTRACT("ONBOARDING_CONTRACT", "入职合同"),
    NDA("NDA", "保密协议"),
    REGULARIZATION_FORM("REGULARIZATION_FORM", "转正申请表"),
    SALARY_ADJUSTMENT_FORM("SALARY_ADJUSTMENT_FORM", "调薪申请表"),
    RENEWAL_CONTRACT("RENEWAL_CONTRACT", "续签合同"),
    DIPLOMA("DIPLOMA", "毕业证书"),
    DEGREE_CERTIFICATE("DEGREE_CERTIFICATE", "学位证书");

    private final String code;
    private final String label;

    AttachmentType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static AttachmentType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (AttachmentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
