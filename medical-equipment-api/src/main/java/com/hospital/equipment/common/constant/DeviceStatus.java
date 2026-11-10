package com.hospital.equipment.common.constant;

/**
 * 设备状态枚举
 */
public enum DeviceStatus {
    NORMAL(1, "正常使用"),
    REPAIRING(2, "维修中"),
    DISABLED(3, "停用/报废"),
    VERIFYING(4, "检定中");

    private final Integer code;
    private final String desc;

    DeviceStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }

    public static DeviceStatus of(Integer code) {
        if (code == null) return null;
        for (DeviceStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        return null;
    }
}
