package com.example.study11.entity.enums;

/** 面谈类型。数据库存 code 字符串，不是 ordinal。 */
public enum InterviewType {

    INTERVIEW("INTERVIEW", "面试应聘"),
    RETEST("RETEST", "复试"),
    ONBOARDING("ONBOARDING", "入职"),
    CUSTOMER_INTERVIEW("CUSTOMER_INTERVIEW", "合作客户面试"),
    PROBATION_CONFIRM("PROBATION_CONFIRM", "转正"),
    SALARY_ADJUSTMENT("SALARY_ADJUSTMENT", "调薪"),
    RETURN_TO_OFFICE("RETURN_TO_OFFICE", "返岗面谈"),
    DEPARTURE("DEPARTURE", "离职");

    private final String code;
    private final String label;

    InterviewType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static InterviewType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (InterviewType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
