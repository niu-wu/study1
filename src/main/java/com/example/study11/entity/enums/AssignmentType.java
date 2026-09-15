package com.example.study11.entity.enums;

/** 稼动事件类型。ENTER 进入客户，RETURN 回公司。 */
public enum AssignmentType {

    ENTER("ENTER"),
    RETURN("RETURN");

    private final String code;

    AssignmentType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static AssignmentType fromCode(String code) {
        for (AssignmentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
