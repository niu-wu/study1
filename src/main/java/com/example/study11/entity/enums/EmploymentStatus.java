package com.example.study11.entity.enums;

/** 在职状态。数据库保存枚举代码，不是 user.role。 */
public enum EmploymentStatus {

    PROBATION("PROBATION"),
    REGULAR("REGULAR"),
    RESIGNED("RESIGNED");

    private final String code;

    EmploymentStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static EmploymentStatus fromCode(String code) {
        for (EmploymentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
