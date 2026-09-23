package com.example.study11.mini.entity.enums;

/** 面试方式。 */
public enum InterviewMode {

    OFFLINE("OFFLINE", "线下面试"),
    VIDEO("VIDEO", "视频面试");

    private final String code;
    private final String label;

    InterviewMode(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static InterviewMode fromCode(String code) {
        if (code == null) return null;
        for (InterviewMode m : values()) if (m.code.equals(code)) return m;
        return null;
    }
}
