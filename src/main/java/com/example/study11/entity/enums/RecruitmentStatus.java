package com.example.study11.entity.enums;

/** 招聘记录状态。数据库保存枚举代码，避免持久化 Java 序号。 */
public enum RecruitmentStatus {

    PENDING_INITIAL("PENDING_INITIAL", false),
    RETEST_REVIEW("RETEST_REVIEW", false),
    PENDING_RETEST("PENDING_RETEST", false),
    PENDING_ONBOARDING("PENDING_ONBOARDING", false),
    ONBOARDED("ONBOARDED", true),
    REJECTED("REJECTED", true),
    DECLINED("DECLINED", true);

    private final String code;

    private final boolean terminal;

    RecruitmentStatus(String code, boolean terminal) {
        this.code = code;
        this.terminal = terminal;
    }

    public String getCode() {
        return code;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public static RecruitmentStatus fromCode(String code) {
        for (RecruitmentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
