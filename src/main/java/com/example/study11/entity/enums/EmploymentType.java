package com.example.study11.entity.enums;

/** 用工类型。数据库保存枚举代码。 */
public enum EmploymentType {

    FULL_TIME("FULL_TIME"),
    PART_TIME("PART_TIME");

    private final String code;

    EmploymentType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static EmploymentType fromCode(String code) {
        for (EmploymentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
