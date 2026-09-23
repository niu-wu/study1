package com.example.study11.mini.entity.enums;

/** 应聘渠道。 */
public enum ApplyChannel {

    REFERRAL("REFERRAL", "内推"),
    BOSS("BOSS", "boss直聘"),
    ZHILIAN("ZHILIAN", "智联"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String label;

    ApplyChannel(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static ApplyChannel fromCode(String code) {
        if (code == null) return null;
        for (ApplyChannel c : values()) if (c.code.equals(code)) return c;
        return null;
    }
}
