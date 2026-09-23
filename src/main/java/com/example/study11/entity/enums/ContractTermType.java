package com.example.study11.entity.enums;

/** 合同期限。数据库存 code 字符串。 */
public enum ContractTermType {

    FIXED("FIXED", "有固定期限"),
    UNLIMITED("UNLIMITED", "无固定期限");

    private final String code;
    private final String label;

    ContractTermType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static ContractTermType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ContractTermType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
