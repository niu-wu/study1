package com.example.study11.entity.enums;

/** 录用通知状态。 */
public enum NoticeStatus {

    DRAFT("DRAFT"),
    SENT("SENT");

    private final String code;

    NoticeStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static NoticeStatus fromCode(String code) {
        for (NoticeStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
