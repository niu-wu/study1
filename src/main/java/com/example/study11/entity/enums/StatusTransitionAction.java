package com.example.study11.entity.enums;

/** 招聘状态变更动作。 */
public enum StatusTransitionAction {

    SUBMIT_RETEST_REVIEW("SUBMIT_RETEST_REVIEW"),
    CONFIRM_RETEST("CONFIRM_RETEST"),
    COMPLETE_RETEST("COMPLETE_RETEST"),
    COMPLETE_ONBOARDING("COMPLETE_ONBOARDING"),
    REJECT("REJECT"),
    DECLINE("DECLINE");

    private final String code;

    StatusTransitionAction(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
