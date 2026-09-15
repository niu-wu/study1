package com.example.study11.entity.enums;

/** 工作地点。仅在职计入总部/外派统计。 */
public enum WorkLocation {

    HEADQUARTERS("HEADQUARTERS"),
    DISPATCHED("DISPATCHED");

    private final String code;

    WorkLocation(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static WorkLocation fromCode(String code) {
        for (WorkLocation location : values()) {
            if (location.code.equals(code)) {
                return location;
            }
        }
        return null;
    }
}
