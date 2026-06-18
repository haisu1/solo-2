package com.office.supplies.common;

public enum WarningLevel {

    NORMAL("NORMAL", "正常", 0),
    ATTENTION("ATTENTION", "关注", 1),
    WARNING("WARNING", "警告", 2),
    URGENT("URGENT", "紧急", 3);

    private final String code;
    private final String desc;
    private final int priority;

    WarningLevel(String code, String desc, int priority) {
        this.code = code;
        this.desc = desc;
        this.priority = priority;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public int getPriority() {
        return priority;
    }

    public static String getDescByCode(String code) {
        for (WarningLevel level : values()) {
            if (level.code.equals(code)) {
                return level.desc;
            }
        }
        return "未知";
    }

    public static WarningLevel getByStockDays(int stockDays, int safeStockDays) {
        if (stockDays <= 0) {
            return URGENT;
        }
        double ratio = (double) stockDays / safeStockDays;
        if (ratio <= 0.3) {
            return URGENT;
        } else if (ratio <= 0.6) {
            return WARNING;
        } else if (ratio <= 1.0) {
            return ATTENTION;
        } else {
            return NORMAL;
        }
    }
}
