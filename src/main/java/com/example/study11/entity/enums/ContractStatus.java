package com.example.study11.entity.enums;

/** 合同状态。 */
public enum ContractStatus {

    EXECUTING("EXECUTING", "执行中"),
    EXPIRED("EXPIRED", "已到期");

    private final String code;
    private final String label;

    ContractStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ContractStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ContractStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
