package com.office.supplies.controller;

import com.office.supplies.common.Result;
import com.office.supplies.dto.ApprovalTaskDTO;
import com.office.supplies.dto.BatchApprovalDTO;
import com.office.supplies.entity.ApprovalRecord;
import com.office.supplies.service.ApprovalEngineService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/approval/task")
public class ApprovalTaskController {

    @Resource
    private ApprovalEngineService approvalEngineService;

    @GetMapping("/mytasks")
    public Result<List<ApprovalTaskDTO>> getMyPendingTasks(
            @RequestParam(required = false) String bizType,
            @RequestParam(defaultValue = "createTime") String sortBy) {
        return Result.success(approvalEngineService.getMyPendingTasks(bizType, sortBy));
    }

    @GetMapping("/history")
    public Result<List<ApprovalRecord>> getApprovalHistory(
            @RequestParam String bizType,
            @RequestParam Long bizId) {
        return Result.success(approvalEngineService.getApprovalHistory(bizType, bizId));
    }

    @PostMapping("/approve")
    public Result<Void> approve(@RequestParam String bizType,
                                @RequestParam Long bizId,
                                @RequestParam String action,
                                @RequestParam(required = false) String remark,
                                @RequestParam(required = false) Long transferToUserId) {
        approvalEngineService.approve(bizType, bizId, action, remark, transferToUserId);
        return Result.success();
    }

    @PostMapping("/withdraw")
    public Result<Void> withdraw(@RequestParam String bizType,
                                 @RequestParam Long bizId,
                                 @RequestParam(required = false) String remark) {
        approvalEngineService.withdraw(bizType, bizId, remark);
        return Result.success();
    }

    @PostMapping("/batch")
    public Result<Void> batchApprove(@RequestBody BatchApprovalDTO dto) {
        approvalEngineService.batchApprove(dto);
        return Result.success();
    }
}
