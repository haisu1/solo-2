package com.office.supplies.controller;

import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.common.Result;
import com.office.supplies.entity.Requisition;
import com.office.supplies.service.RequisitionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/requisitions")
public class RequisitionController {

    @Resource
    private RequisitionService requisitionService;

    @GetMapping("/page")
    public Result<PageResult<Requisition>> getRequisitionPage(
            @Valid PageQuery query,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId) {
        return Result.success(requisitionService.getRequisitionPage(query, status, departmentId));
    }

    @GetMapping("/{id}")
    public Result<Requisition> getRequisitionDetail(@PathVariable Long id) {
        return Result.success(requisitionService.getRequisitionDetail(id));
    }

    @PostMapping
    public Result<Requisition> createRequisition(@RequestBody Requisition requisition) {
        try {
            Requisition result = requisitionService.createRequisition(requisition);
            return Result.success("提交成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approveRequisition(
            @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        String status = params.get("status");
        String remark = params.get("remark");
        if (status == null || (!"APPROVED".equals(status) && !"REJECTED".equals(status))) {
            return Result.error("审批状态不正确");
        }
        try {
            requisitionService.approveRequisition(id, status, remark);
            return Result.success("APPROVED".equals(status) ? "审批通过" : "审批驳回");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancelRequisition(@PathVariable Long id) {
        try {
            requisitionService.cancelRequisition(id);
            return Result.success("取消成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
