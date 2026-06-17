package com.office.supplies.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.approval.ApprovalPermissionChecker;
import com.office.supplies.approval.ApprovalStateMachine;
import com.office.supplies.common.*;
import com.office.supplies.dto.ApprovalTaskDTO;
import com.office.supplies.dto.BatchApprovalDTO;
import com.office.supplies.entity.*;
import com.office.supplies.mapper.ApprovalRecordMapper;
import com.office.supplies.mapper.RequisitionItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApprovalEngineService extends ServiceImpl<ApprovalRecordMapper, ApprovalRecord> {

    @Resource
    private ApprovalRecordMapper approvalRecordMapper;

    @Resource
    private ApprovalFlowService approvalFlowService;

    @Resource
    private ApprovalPermissionChecker permissionChecker;

    @Resource
    private UserService userService;

    @Resource
    private RequisitionService requisitionService;

    @Resource
    private PurchaseService purchaseService;

    @Resource
    private SupplyService supplyService;

    @Resource
    private RequisitionItemMapper requisitionItemMapper;

    private final ApprovalStateMachine stateMachine = new ApprovalStateMachine();

    public ApprovalStateMachine getStateMachine() {
        return stateMachine;
    }

    @Transactional(rollbackFor = Exception.class)
    public void initApproval(String bizType, Long bizId, String bizNo,
                             Long applicantId, Long departmentId,
                             Long categoryId, BigDecimal totalAmount) {
        ApprovalFlow flow = approvalFlowService.getMatchedFlow(bizType, departmentId, categoryId, totalAmount);
        List<ApprovalNode> nodes = flow.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            nodes = new ArrayList<>();
        }
        int totalLevels = nodes.size();
        if (totalLevels == 0) {
            totalLevels = 1;
        }
        ApprovalNode firstNode = totalLevels > 0 ? nodes.get(0) : createDefaultNode();
        createApprovalRecord(bizType, bizId, bizNo, flow.getId(), 1, totalLevels, firstNode, applicantId);
    }

    private ApprovalNode createDefaultNode() {
        ApprovalNode node = new ApprovalNode();
        node.setId(0L);
        node.setFlowId(0L);
        node.setNodeLevel(1);
        node.setNodeName("主管审批");
        node.setApproverType("DEPARTMENT_LEADER");
        node.setTimeoutHours(24);
        node.setCanTransfer(1);
        node.setCanWithdraw(1);
        return node;
    }

    private void createApprovalRecord(String bizType, Long bizId, String bizNo,
                                      Long flowId, int level, int totalLevels,
                                      ApprovalNode node, Long applicantId) {
        List<Long> approverIds = permissionChecker.getNodeApproverIds(node);
        if (approverIds.isEmpty()) {
            ApprovalRecord record = buildRecord(bizType, bizId, bizNo, flowId, level, totalLevels, node);
            record.setApproverId(0L);
            record.setApproverName("系统自动审批");
            record.setAction(ApprovalAction.APPROVE);
            record.setStartTime(LocalDateTime.now());
            record.setApproveTime(LocalDateTime.now());
            record.setRemark("无审批人配置，系统自动通过");
            approvalRecordMapper.insert(record);
            advanceToNextLevel(bizType, bizId, bizNo, flowId, level, totalLevels, applicantId, "系统自动审批");
            return;
        }
        for (Long approverId : approverIds) {
            if (applicantId != null && approverId.equals(applicantId)) {
                continue;
            }
            ApprovalRecord record = buildRecord(bizType, bizId, bizNo, flowId, level, totalLevels, node);
            record.setApproverId(approverId);
            User user = userService.getById(approverId);
            if (user != null) {
                record.setApproverName(user.getRealName());
            }
            record.setStartTime(LocalDateTime.now());
            approvalRecordMapper.insert(record);
        }
    }

    private ApprovalRecord buildRecord(String bizType, Long bizId, String bizNo,
                                       Long flowId, int level, int totalLevels, ApprovalNode node) {
        ApprovalRecord record = new ApprovalRecord();
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setBizNo(bizNo);
        record.setFlowId(flowId);
        record.setCurrentLevel(level);
        record.setTotalLevels(totalLevels);
        if (node != null) {
            record.setNodeName(node.getNodeName());
        }
        return record;
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(String bizType, Long bizId, String action, String remark, Long transferToUserId) {
        User currentUser = UserContext.getCurrentUser();
        Long currentUserId = currentUser.getId();

        List<ApprovalRecord> records = approvalRecordMapper.getRecordsByBiz(bizType, bizId);
        List<ApprovalRecord> currentLevelRecords = records.stream()
                .filter(r -> r.getAction() == null)
                .collect(Collectors.toList());

        if (currentLevelRecords.isEmpty()) {
            throw new RuntimeException("当前没有待审批的任务");
        }

        ApprovalRecord myRecord = currentLevelRecords.stream()
                .filter(r -> currentUserId.equals(r.getApproverId()))
                .findFirst()
                .orElse(null);

        if (myRecord == null) {
            throw new RuntimeException("您没有该单据的审批权限");
        }

        int currentLevel = myRecord.getCurrentLevel();
        int totalLevels = myRecord.getTotalLevels();
        Long applicantId = getApplicantId(bizType, bizId);
        Long flowId = myRecord.getFlowId();
        String bizNo = myRecord.getBizNo();

        checkPermission(bizType, bizId, currentUserId, applicantId, currentLevel, flowId);

        myRecord.setAction(action);
        myRecord.setRemark(remark);
        myRecord.setApproveTime(LocalDateTime.now());
        approvalRecordMapper.updateById(myRecord);

        for (ApprovalRecord r : currentLevelRecords) {
            if (!r.getId().equals(myRecord.getId()) && r.getAction() == null) {
                r.setAction("SKIPPED");
                r.setRemark("其他审批人已处理");
                r.setApproveTime(LocalDateTime.now());
                approvalRecordMapper.updateById(r);
            }
        }

        if (ApprovalAction.TRANSFER.equals(action)) {
            handleTransfer(bizType, bizId, bizNo, flowId, currentLevel, totalLevels, myRecord, transferToUserId);
            return;
        }

        if (ApprovalAction.REJECT.equals(action)) {
            updateBizStatus(bizType, bizId, ApprovalStatus.REJECTED, currentUserId, remark);
            return;
        }

        if (ApprovalAction.APPROVE.equals(action)) {
            advanceToNextLevel(bizType, bizId, bizNo, flowId, currentLevel, totalLevels, applicantId, remark);
        }
    }

    private void handleTransfer(String bizType, Long bizId, String bizNo, Long flowId,
                                int currentLevel, int totalLevels, ApprovalRecord originalRecord,
                                Long transferToUserId) {
        if (transferToUserId == null) {
            throw new RuntimeException("请指定转交对象");
        }
        User transferUser = userService.getById(transferToUserId);
        if (transferUser == null) {
            throw new RuntimeException("转交用户不存在");
        }
        ApprovalFlow flow = flowId != null && flowId > 0 ? approvalFlowService.getById(flowId) : null;
        ApprovalNode node = null;
        if (flow != null) {
            node = approvalFlowService.parseNodeConfig(flow.getNodeConfig()).stream()
                    .filter(n -> n.getNodeLevel() != null && n.getNodeLevel() == currentLevel)
                    .findFirst().orElse(null);
            if (node == null) {
                List<ApprovalNode> dbNodes = com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(ApprovalNode.class) != null
                        ? new ArrayList<>() : new ArrayList<>();
            }
        }
        ApprovalRecord newRecord = new ApprovalRecord();
        newRecord.setBizType(bizType);
        newRecord.setBizId(bizId);
        newRecord.setBizNo(bizNo);
        newRecord.setFlowId(flowId);
        newRecord.setCurrentLevel(currentLevel);
        newRecord.setTotalLevels(totalLevels);
        newRecord.setNodeName(originalRecord.getNodeName());
        newRecord.setApproverId(transferToUserId);
        newRecord.setApproverName(transferUser.getRealName());
        newRecord.setStartTime(LocalDateTime.now());
        newRecord.setTransferFromId(originalRecord.getApproverId());
        newRecord.setTransferFromName(originalRecord.getApproverName());
        approvalRecordMapper.insert(newRecord);
    }

    private void advanceToNextLevel(String bizType, Long bizId, String bizNo, Long flowId,
                                    int currentLevel, int totalLevels, Long applicantId, String remark) {
        User currentUser = UserContext.getCurrentUser();
        if (currentLevel >= totalLevels) {
            updateBizStatus(bizType, bizId, ApprovalStatus.APPROVED, currentUser.getId(), remark);
            handleFinalApproval(bizType, bizId);
            return;
        }
        int nextLevel = currentLevel + 1;
        ApprovalNode nextNode = getNodeByLevel(flowId, nextLevel);
        if (nextNode == null) {
            updateBizStatus(bizType, bizId, ApprovalStatus.APPROVED, currentUser.getId(), remark);
            handleFinalApproval(bizType, bizId);
            return;
        }
        createApprovalRecord(bizType, bizId, bizNo, flowId, nextLevel, totalLevels, nextNode, applicantId);
        updateBizStatus(bizType, bizId, ApprovalStatus.getPendingLevel(nextLevel), currentUser.getId(), remark);
    }

    private ApprovalNode getNodeByLevel(Long flowId, int level) {
        if (flowId == null || flowId <= 0) {
            return null;
        }
        ApprovalFlow flow = approvalFlowService.getById(flowId);
        if (flow == null) {
            return null;
        }
        List<ApprovalNode> nodes = approvalFlowService.parseNodeConfig(flow.getNodeConfig());
        if (nodes != null && !nodes.isEmpty()) {
            return nodes.stream()
                    .filter(n -> n.getNodeLevel() != null && n.getNodeLevel() == level)
                    .findFirst().orElse(null);
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdraw(String bizType, Long bizId, String remark) {
        User currentUser = UserContext.getCurrentUser();
        Long currentUserId = currentUser.getId();

        List<ApprovalRecord> records = approvalRecordMapper.getRecordsByBiz(bizType, bizId);
        ApprovalRecord currentPending = records.stream()
                .filter(r -> r.getAction() == null)
                .findFirst().orElse(null);

        if (currentPending == null) {
            throw new RuntimeException("当前没有待审批节点，无法撤回");
        }

        int currentLevel = currentPending.getCurrentLevel();
        if (currentLevel <= 1) {
            throw new RuntimeException("当前是第一级审批，无法撤回");
        }

        ApprovalRecord lastApproved = records.stream()
                .filter(r -> ApprovalAction.APPROVE.equals(r.getAction())
                        && r.getCurrentLevel() != null
                        && r.getCurrentLevel() == currentLevel - 1)
                .findFirst().orElse(null);

        if (lastApproved == null || !currentUserId.equals(lastApproved.getApproverId())) {
            throw new RuntimeException("只有上一级审批人可以撤回");
        }

        Long flowId = currentPending.getFlowId();
        Long applicantId = getApplicantId(bizType, bizId);
        String bizNo = currentPending.getBizNo();
        int totalLevels = currentPending.getTotalLevels();

        for (ApprovalRecord r : records) {
            if (r.getAction() == null && r.getCurrentLevel() == currentLevel) {
                approvalRecordMapper.deleteById(r.getId());
            }
        }
        lastApproved.setAction("WITHDRAWN");
        lastApproved.setRemark("撤回：" + (remark != null ? remark : ""));
        approvalRecordMapper.updateById(lastApproved);

        ApprovalNode prevNode = getNodeByLevel(flowId, currentLevel - 1);
        if (prevNode == null) {
            prevNode = createDefaultNode();
        }
        createApprovalRecord(bizType, bizId, bizNo, flowId, currentLevel - 1, totalLevels, prevNode, applicantId);
        updateBizStatus(bizType, bizId, ApprovalStatus.getPendingLevel(currentLevel - 1), currentUserId, remark);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchApprove(BatchApprovalDTO dto) {
        if (dto.getBizIds() == null || dto.getBizIds().isEmpty()) {
            throw new RuntimeException("请选择要审批的单据");
        }
        for (Long bizId : dto.getBizIds()) {
            approve(dto.getBizType(), bizId, dto.getAction(), dto.getRemark(), dto.getTransferToUserId());
        }
    }

    public List<ApprovalTaskDTO> getMyPendingTasks(String bizType, String sortBy) {
        User currentUser = UserContext.getCurrentUser();
        List<ApprovalRecord> records;
        if (bizType != null && !bizType.isEmpty()) {
            records = approvalRecordMapper.getPendingTasksByApprover(currentUser.getId(), bizType);
        } else {
            records = approvalRecordMapper.getAllPendingTasksByApprover(currentUser.getId());
        }
        List<ApprovalTaskDTO> tasks = new ArrayList<>();
        for (ApprovalRecord record : records) {
            ApprovalTaskDTO task = buildTaskDTO(record);
            tasks.add(task);
        }
        sortTasks(tasks, sortBy);
        return tasks;
    }

    private ApprovalTaskDTO buildTaskDTO(ApprovalRecord record) {
        ApprovalTaskDTO task = new ApprovalTaskDTO();
        task.setId(record.getId());
        task.setBizType(record.getBizType());
        task.setBizId(record.getBizId());
        task.setBizNo(record.getBizNo());
        task.setCurrentLevel(record.getCurrentLevel());
        task.setTotalLevels(record.getTotalLevels());
        task.setNodeName(record.getNodeName());
        task.setApproverId(record.getApproverId() != null ? record.getApproverId().toString() : null);
        task.setApproverName(record.getApproverName());
        task.setStartTime(record.getStartTime());
        Object bizData = getBizData(record.getBizType(), record.getBizId());
        task.setBizData(bizData);
        if (bizData instanceof Requisition) {
            Requisition r = (Requisition) bizData;
            task.setStatus(r.getStatus());
            task.setCreateTime(r.getCreateTime());
            task.setApplicantName(r.getRealName());
            task.setDepartmentName(r.getDepartmentName());
        } else if (bizData instanceof Purchase) {
            Purchase p = (Purchase) bizData;
            task.setStatus(p.getStatus());
            task.setCreateTime(p.getCreateTime());
            task.setApplicantName(p.getCreatedByName());
            task.setTotalAmount(p.getTotalAmount());
        }
        if (record.getStartTime() != null) {
            long timeoutHours = getNodeTimeoutHours(record);
            LocalDateTime deadline = record.getStartTime().plusHours(timeoutHours);
            task.setTimeoutMinutes(Duration.between(LocalDateTime.now(), deadline).toMinutes());
            task.setIsUrgent(task.getTimeoutMinutes() != null && task.getTimeoutMinutes() < 60);
        }
        return task;
    }

    private long getNodeTimeoutHours(ApprovalRecord record) {
        ApprovalNode node = getNodeByLevel(record.getFlowId(), record.getCurrentLevel());
        if (node != null && node.getTimeoutHours() != null) {
            return node.getTimeoutHours();
        }
        return 24;
    }

    private void sortTasks(List<ApprovalTaskDTO> tasks, String sortBy) {
        Comparator<ApprovalTaskDTO> comparator;
        if ("urgent".equals(sortBy)) {
            comparator = Comparator.comparing(t -> t.getIsUrgent() != null && t.getIsUrgent() ? 0 : 1);
            comparator = comparator.thenComparing(t -> t.getTimeoutMinutes() != null ? t.getTimeoutMinutes() : Long.MAX_VALUE);
        } else if ("timeout".equals(sortBy)) {
            comparator = Comparator.comparing(t -> t.getTimeoutMinutes() != null ? t.getTimeoutMinutes() : Long.MAX_VALUE);
        } else {
            comparator = Comparator.comparing(ApprovalTaskDTO::getCreateTime).reversed();
        }
        tasks.sort(comparator);
    }

    public List<ApprovalRecord> getApprovalHistory(String bizType, Long bizId) {
        List<ApprovalRecord> records = approvalRecordMapper.getRecordsByBiz(bizType, bizId);
        for (ApprovalRecord r : records) {
            r.calculateDuration();
        }
        return records;
    }

    private void checkPermission(String bizType, Long bizId, Long currentUserId,
                                 Long applicantId, int level, Long flowId) {
        if (applicantId != null && currentUserId.equals(applicantId)) {
            throw new RuntimeException("不能审批自己提交的单据");
        }
        ApprovalNode node = getNodeByLevel(flowId, level);
        if (node != null && !permissionChecker.isNodeApprover(currentUserId, node)) {
            throw new RuntimeException("您没有该单据的审批权限");
        }
    }

    private Long getApplicantId(String bizType, Long bizId) {
        if (BizType.REQUISITION.equals(bizType)) {
            Requisition r = requisitionService.getById(bizId);
            return r != null ? r.getUserId() : null;
        } else if (BizType.PURCHASE.equals(bizType)) {
            Purchase p = purchaseService.getById(bizId);
            return p != null ? p.getCreatedBy() : null;
        }
        return null;
    }

    private Object getBizData(String bizType, Long bizId) {
        if (BizType.REQUISITION.equals(bizType)) {
            return requisitionService.getRequisitionDetail(bizId);
        } else if (BizType.PURCHASE.equals(bizType)) {
            return purchaseService.getPurchaseDetail(bizId);
        }
        return null;
    }

    private void updateBizStatus(String bizType, Long bizId, String status, Long approverId, String remark) {
        if (BizType.REQUISITION.equals(bizType)) {
            Requisition r = requisitionService.getById(bizId);
            if (r != null) {
                r.setStatus(status);
                r.setApprovedBy(approverId);
                r.setApproveTime(LocalDateTime.now());
                r.setApproveRemark(remark);
                requisitionService.updateById(r);
            }
        } else if (BizType.PURCHASE.equals(bizType)) {
            Purchase p = purchaseService.getById(bizId);
            if (p != null) {
                p.setStatus(status);
                p.setApprovedBy(approverId);
                p.setApproveTime(LocalDateTime.now());
                p.setRemark(remark);
                purchaseService.updateById(p);
            }
        }
    }

    private void handleFinalApproval(String bizType, Long bizId) {
        if (BizType.REQUISITION.equals(bizType)) {
            Requisition r = requisitionService.getById(bizId);
            if (r != null) {
                List<RequisitionItem> items = requisitionItemMapper.getItemsByRequisitionId(bizId);
                Map<Long, Integer> stockRequirements = new HashMap<>();
                for (RequisitionItem item : items) {
                    stockRequirements.merge(item.getSupplyId(), item.getQuantity(), Integer::sum);
                }
                if (!stockRequirements.isEmpty()) {
                    try {
                        supplyService.checkStockAvailability(stockRequirements);
                        supplyService.batchReduceStock(stockRequirements, r.getRequisitionNo(), "领用发放");
                    } catch (Exception e) {
                        throw new RuntimeException("库存处理失败：" + e.getMessage());
                    }
                }
            }
        }
    }

    public void processTimeoutReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeoutThreshold = now.minusHours(24);
        List<ApprovalRecord> timeoutRecords = approvalRecordMapper.getTimeoutTasks(timeoutThreshold);
        for (ApprovalRecord record : timeoutRecords) {
            ApprovalRecord timeoutReminder = new ApprovalRecord();
            timeoutReminder.setBizType(record.getBizType());
            timeoutReminder.setBizId(record.getBizId());
            timeoutReminder.setBizNo(record.getBizNo());
            timeoutReminder.setFlowId(record.getFlowId());
            timeoutReminder.setCurrentLevel(record.getCurrentLevel());
            timeoutReminder.setTotalLevels(record.getTotalLevels());
            timeoutReminder.setNodeName(record.getNodeName() + "-超时提醒");
            timeoutReminder.setAction(ApprovalAction.TIMEOUT_REMIND);
            timeoutReminder.setApproverId(record.getApproverId());
            timeoutReminder.setApproverName(record.getApproverName());
            timeoutReminder.setStartTime(record.getStartTime());
            timeoutReminder.setApproveTime(now);
            timeoutReminder.setRemark("审批已超时，请及时处理");
            approvalRecordMapper.insert(timeoutReminder);
        }
    }
}
