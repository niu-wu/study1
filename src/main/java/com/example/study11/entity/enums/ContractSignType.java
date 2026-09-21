package com.example.study11.entity.enums;

/** 合同签订类型。 */
public enum ContractSignType {

    NEW("NEW", "新签"),
    RENEWAL("RENEWAL", "续签");

    private final String code;
    private final String label;

    ContractSignType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ContractSignType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ContractSignType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
