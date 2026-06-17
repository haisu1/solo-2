package com.office.supplies.common;

public class ApprovalStatus {
    public static final String DRAFT = "DRAFT";
    public static final String PENDING = "PENDING";
    public static final String PENDING_LEVEL = "PENDING_LEVEL";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";
    public static final String CANCELLED = "CANCELLED";
    public static final String STOCKED = "STOCKED";

    public static String getPendingLevel(int level) {
        return PENDING_LEVEL + level;
    }

    public static boolean isPendingStatus(String status) {
        return status != null && (status.startsWith(PENDING_LEVEL) || PENDING.equals(status));
    }

    public static int getCurrentLevel(String status) {
        if (status != null && status.startsWith(PENDING_LEVEL)) {
            try {
                return Integer.parseInt(status.substring(PENDING_LEVEL.length()));
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }
}
