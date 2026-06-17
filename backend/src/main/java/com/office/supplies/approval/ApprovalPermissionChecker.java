package com.office.supplies.approval;

import com.office.supplies.common.ApprovalStatus;
import com.office.supplies.entity.ApprovalNode;
import com.office.supplies.entity.User;
import com.office.supplies.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ApprovalPermissionChecker {

    @Resource
    private UserService userService;

    public boolean canApprove(Long currentUserId, Long applicantId, ApprovalNode node) {
        if (currentUserId == null) {
            return false;
        }
        if (applicantId != null && currentUserId.equals(applicantId)) {
            return false;
        }
        return isNodeApprover(currentUserId, node);
    }

    public boolean isNodeApprover(Long userId, ApprovalNode node) {
        if (node == null || userId == null) {
            return false;
        }
        String approverType = node.getApproverType();
        if ("USER".equals(approverType)) {
            return checkUserApprover(userId, node.getApproverIds());
        } else if ("ROLE".equals(approverType)) {
            return checkRoleApprover(userId, node.getRoleCode());
        } else if ("DEPARTMENT_LEADER".equals(approverType)) {
            return checkDepartmentLeader(userId);
        }
        return false;
    }

    private boolean checkUserApprover(Long userId, String approverIds) {
        if (approverIds == null || approverIds.isEmpty()) {
            return false;
        }
        List<String> ids = Arrays.asList(approverIds.split(","));
        return ids.contains(userId.toString());
    }

    private boolean checkRoleApprover(Long userId, String roleCode) {
        if (roleCode == null || userId == null) {
            return false;
        }
        User user = userService.getById(userId);
        if (user == null || user.getRoleId() == null) {
            return false;
        }
        com.office.supplies.entity.Role role = userService.getRoleById(user.getRoleId());
        return role != null && roleCode.equals(role.getRoleCode());
    }

    private boolean checkDepartmentLeader(Long userId) {
        if (userId == null) {
            return false;
        }
        User user = userService.getById(userId);
        if (user == null || user.getRoleId() == null) {
            return false;
        }
        com.office.supplies.entity.Role role = userService.getRoleById(user.getRoleId());
        if (role == null) {
            return false;
        }
        return "DEPT_MANAGER".equals(role.getRoleCode()) || "ADMIN".equals(role.getRoleCode());
    }

    public List<Long> getNodeApproverIds(ApprovalNode node) {
        List<Long> ids = new ArrayList<>();
        if (node == null) {
            return ids;
        }
        String approverType = node.getApproverType();
        if ("USER".equals(approverType)) {
            if (node.getApproverIds() != null && !node.getApproverIds().isEmpty()) {
                ids = Arrays.stream(node.getApproverIds().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
            }
        } else if ("ROLE".equals(approverType)) {
            ids = userService.getUserIdsByRoleCode(node.getRoleCode());
        } else if ("DEPARTMENT_LEADER".equals(approverType)) {
            ids = userService.getDepartmentLeaderIds();
        }
        return ids;
    }

    public boolean canWithdraw(Long currentUserId, ApprovalNode currentNode,
                               ApprovalRecordSummary lastApprovedRecord) {
        if (currentUserId == null || lastApprovedRecord == null) {
            return false;
        }
        if (currentNode != null && currentNode.getCanWithdraw() != null && currentNode.getCanWithdraw() == 0) {
            return false;
        }
        return currentUserId.equals(lastApprovedRecord.getApproverId());
    }

    public boolean canTransfer(Long currentUserId, ApprovalNode currentNode, Long currentApproverId) {
        if (currentUserId == null) {
            return false;
        }
        if (!currentUserId.equals(currentApproverId)) {
            return false;
        }
        if (currentNode != null && currentNode.getCanTransfer() != null && currentNode.getCanTransfer() == 0) {
            return false;
        }
        return true;
    }

    public static class ApprovalRecordSummary {
        private Long approverId;
        private Integer level;
        private String action;

        public Long getApproverId() { return approverId; }
        public void setApproverId(Long approverId) { this.approverId = approverId; }
        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    }
}
