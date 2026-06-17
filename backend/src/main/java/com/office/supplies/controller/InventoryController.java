package com.office.supplies.controller;

import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.common.Result;
import com.office.supplies.entity.InventoryCheck;
import com.office.supplies.entity.InventoryCheckItem;
import com.office.supplies.entity.StockLog;
import com.office.supplies.service.InventoryCheckService;
import com.office.supplies.service.StockLogService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Resource
    private InventoryCheckService inventoryCheckService;

    @Resource
    private StockLogService stockLogService;

    @GetMapping("/check/page")
    public Result<PageResult<InventoryCheck>> getCheckPage(
            @Valid PageQuery query,
            @RequestParam(required = false) String status) {
        return Result.success(inventoryCheckService.getCheckPage(query, status));
    }

    @GetMapping("/check/{id}")
    public Result<InventoryCheck> getCheckDetail(@PathVariable Long id) {
        return Result.success(inventoryCheckService.getCheckDetail(id));
    }

    @PostMapping("/check")
    public Result<InventoryCheck> createCheck(@RequestBody InventoryCheck check) {
        try {
            InventoryCheck result = inventoryCheckService.createCheck(check);
            return Result.success("创建成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/check/{checkId}/items")
    public Result<Void> updateCheckItems(
            @PathVariable Long checkId,
            @RequestBody List<InventoryCheckItem> items) {
        try {
            inventoryCheckService.updateCheckItems(checkId, items);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/check/{id}/complete")
    public Result<Void> completeCheck(@PathVariable Long id) {
        try {
            inventoryCheckService.completeCheck(id);
            return Result.success("盘点完成");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/stock-logs")
    public Result<List<StockLog>> getStockLogs(
            @RequestParam(required = false) Long supplyId,
            @RequestParam(required = false) String operationType) {
        return Result.success(stockLogService.getStockLogList(supplyId, operationType));
    }
}
