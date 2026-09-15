package com.example.study11.entity.enums;

/** 入职登记表状态。数据库保存枚举代码。 */
public enum EmployeeFormStatus {

    DRAFT("DRAFT"),
    SUBMITTED("SUBMITTED");

    private final String code;

    EmployeeFormStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static EmployeeFormStatus fromCode(String code) {
        for (EmployeeFormStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
