package com.hospital.equipment.common.constant;

/**
 * 工单状态枚举
 */
public enum OrderStatus {
    PENDING(1, "待受理"),
    REPAIRING(2, "维修中"),
    PENDING_RATE(3, "待评价"),
    FINISHED(4, "已完成"),
    CLOSED(5, "已关闭");

    private final Integer code;
    private final String desc;

    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }

    public static OrderStatus of(Integer code) {
        if (code == null) return null;
        for (OrderStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        return null;
    }
}
