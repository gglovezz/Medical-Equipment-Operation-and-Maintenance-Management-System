package com.hospital.equipment.common.constant;

/**
 * 角色枚举
 */
public enum RoleEnum {
    ADMIN("admin", "管理员"),
    ENGINEER("engineer", "设备科工程师"),
    NURSE("nurse", "临床护士");

    private final String code;
    private final String desc;

    RoleEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static RoleEnum of(String code) {
        for (RoleEnum r : values()) {
            if (r.code.equals(code)) return r;
        }
        return null;
    }
}
