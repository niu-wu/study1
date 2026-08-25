package com.example.study11.entity.enums;

/** 人才库分类。 */
public enum TalentCategory {

    FUTURE_CONSIDER("FUTURE_CONSIDER"),
    SKILL_BACKUP("SKILL_BACKUP"),
    NOT_SUITABLE("NOT_SUITABLE");

    private final String code;

    TalentCategory(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
