package com.example.study11.entity.enums;

/** 淘汰或放弃入职时所处的招聘阶段。 */
public enum RejectionStage {

    INITIAL_INTERVIEW("INITIAL_INTERVIEW"),
    RETEST("RETEST"),
    BACKGROUND_CHECK("BACKGROUND_CHECK"),
    ONBOARDING("ONBOARDING");

    private final String code;

    RejectionStage(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
