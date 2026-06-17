package com.office.supplies.approval;

import com.office.supplies.common.ApprovalStatus;

import java.util.*;

public class ApprovalStateMachine {

    private final Map<String, Set<String>> transitions = new HashMap<>();

    public ApprovalStateMachine() {
        buildTransitions();
    }

    private void buildTransitions() {
        addTransition(ApprovalStatus.DRAFT, ApprovalStatus.PENDING_LEVEL + "1");
        addTransition(ApprovalStatus.PENDING, ApprovalStatus.PENDING_LEVEL + "1");
        for (int i = 1; i <= 10; i++) {
            addTransition(ApprovalStatus.PENDING_LEVEL + i, ApprovalStatus.REJECTED);
            if (i < 10) {
                addTransition(ApprovalStatus.PENDING_LEVEL + i, ApprovalStatus.PENDING_LEVEL + (i + 1));
            }
        }
        addTransition(ApprovalStatus.PENDING_LEVEL + "10", ApprovalStatus.APPROVED);
        for (int i = 1; i <= 9; i++) {
            addTransition(ApprovalStatus.PENDING_LEVEL + (i + 1), ApprovalStatus.PENDING_LEVEL + i);
        }
        for (int i = 1; i <= 10; i++) {
            addTransition(ApprovalStatus.PENDING_LEVEL + i, ApprovalStatus.CANCELLED);
        }
    }

    private void addTransition(String from, String to) {
        transitions.computeIfAbsent(from, k -> new HashSet<>()).add(to);
    }

    public boolean canTransition(String currentStatus, String targetStatus) {
        if (ApprovalStatus.APPROVED.equals(targetStatus)) {
            if (currentStatus != null && currentStatus.startsWith(ApprovalStatus.PENDING_LEVEL)) {
                return true;
            }
        }
        Set<String> allowed = transitions.get(currentStatus);
        return allowed != null && allowed.contains(targetStatus);
    }

    public String getNextLevelStatus(String currentStatus) {
        int level = ApprovalStatus.getCurrentLevel(currentStatus);
        return ApprovalStatus.getPendingLevel(level + 1);
    }

    public String getPrevLevelStatus(String currentStatus) {
        int level = ApprovalStatus.getCurrentLevel(currentStatus);
        if (level <= 1) {
            return ApprovalStatus.PENDING_LEVEL + "1";
        }
        return ApprovalStatus.getPendingLevel(level - 1);
    }

    public String getApprovedStatus(int totalLevels, String currentStatus) {
        int currentLevel = ApprovalStatus.getCurrentLevel(currentStatus);
        if (currentLevel >= totalLevels) {
            return ApprovalStatus.APPROVED;
        }
        return ApprovalStatus.getPendingLevel(currentLevel + 1);
    }

    public boolean isFinalStatus(String status) {
        return ApprovalStatus.APPROVED.equals(status)
                || ApprovalStatus.REJECTED.equals(status)
                || ApprovalStatus.CANCELLED.equals(status)
                || ApprovalStatus.STOCKED.equals(status);
    }
}
