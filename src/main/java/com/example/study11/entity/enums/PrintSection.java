package com.example.study11.entity.enums;

/** 打印预览可选模块。不传时不使用本枚举，响应保持原样。 */
public enum PrintSection {

    BASIC("basic"),
    EXPERIENCE("experience"),
    FAMILY("family"),
    EMERGENCY("emergency"),
    SALARY("salary"),
    ACCOUNT("account"),
    ASSIGNMENT("assignment"),
    INTERVIEW("interview"),
    CONTRACT("contract"),
    ATTACHMENT("attachment");

    private final String code;

    PrintSection(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static PrintSection fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (PrintSection section : values()) {
            if (section.code.equals(code)) {
                return section;
            }
        }
        return null;
    }
}
