package com.office.supplies.controller;

import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.common.Result;
import com.office.supplies.entity.Purchase;
import com.office.supplies.service.PurchaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    @Resource
    private PurchaseService purchaseService;

    @GetMapping("/page")
    public Result<PageResult<Purchase>> getPurchasePage(
            @Valid PageQuery query,
            @RequestParam(required = false) String status) {
        return Result.success(purchaseService.getPurchasePage(query, status));
    }

    @GetMapping("/{id}")
    public Result<Purchase> getPurchaseDetail(@PathVariable Long id) {
        return Result.success(purchaseService.getPurchaseDetail(id));
    }

    @PostMapping
    public Result<Purchase> createPurchase(@RequestBody Purchase purchase) {
        try {
            Purchase result = purchaseService.createPurchase(purchase);
            return Result.success("创建成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approvePurchase(
            @PathVariable Long id,
            @RequestBody Map<String, String> params) {
        String status = params.get("status");
        String remark = params.get("remark");
        if (status == null || (!"APPROVED".equals(status) && !"REJECTED".equals(status))) {
            return Result.error("审批状态不正确");
        }
        try {
            purchaseService.approvePurchase(id, status, remark);
            return Result.successMsg("APPROVED".equals(status) ? "审批通过" : "审批驳回");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/stock-in")
    public Result<Void> stockIn(@PathVariable Long id) {
        try {
            purchaseService.stockIn(id);
            return Result.successMsg("入库成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/withdraw")
    public Result<Void> withdrawApproval(@PathVariable Long id,
                                         @RequestBody(required = false) Map<String, String> params) {
        try {
            String remark = params != null ? params.get("remark") : null;
            purchaseService.withdrawApproval(id, remark);
            return Result.successMsg("撤回成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/transfer")
    public Result<Void> transferApproval(@PathVariable Long id,
                                         @RequestBody Map<String, Object> params) {
        try {
            String remark = (String) params.get("remark");
            Long transferToUserId = params.get("transferToUserId") != null
                    ? Long.valueOf(params.get("transferToUserId").toString()) : null;
            purchaseService.transferApproval(id, remark, transferToUserId);
            return Result.successMsg("转交成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
