package com.example.study11.entity.enums;

/** 淘汰或放弃入职原因。 */
public enum RejectionReason {

    SKILL_MISMATCH("SKILL_MISMATCH"),
    CULTURE_MISMATCH("CULTURE_MISMATCH"),
    SALARY_EXPECTATION("SALARY_EXPECTATION"),
    OTHER("OTHER"),
    CANDIDATE_DECLINED("CANDIDATE_DECLINED");

    private final String code;

    RejectionReason(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
