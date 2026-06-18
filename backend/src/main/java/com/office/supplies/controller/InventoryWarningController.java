package com.office.supplies.controller;

import com.office.supplies.common.Result;
import com.office.supplies.common.UserContext;
import com.office.supplies.dto.ConsumptionTrendDTO;
import com.office.supplies.dto.PurchaseSuggestionDTO;
import com.office.supplies.dto.SupplyWarningDTO;
import com.office.supplies.dto.WarningMessageDTO;
import com.office.supplies.entity.Purchase;
import com.office.supplies.entity.User;
import com.office.supplies.service.InventoryWarningService;
import com.office.supplies.service.PurchaseSuggestionService;
import com.office.supplies.service.WarningNotificationService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warnings")
public class InventoryWarningController {

    @Resource
    private InventoryWarningService inventoryWarningService;

    @Resource
    private WarningNotificationService warningNotificationService;

    @Resource
    private PurchaseSuggestionService purchaseSuggestionService;

    @GetMapping("/supplies")
    public Result<List<SupplyWarningDTO>> getSupplyWarnings(
            @RequestParam(required = false) String warningLevel) {
        return Result.success(inventoryWarningService.getAllWarnings(warningLevel));
    }

    @GetMapping("/supplies/warning-only")
    public Result<List<SupplyWarningDTO>> getWarningSupplies() {
        return Result.success(inventoryWarningService.getWarningSupplies());
    }

    @GetMapping("/supplies/{supplyId}")
    public Result<SupplyWarningDTO> getSupplyWarning(@PathVariable Long supplyId) {
        return Result.success(inventoryWarningService.getSupplyWarning(supplyId));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getWarningStatistics() {
        return Result.success(inventoryWarningService.getWarningStatistics());
    }

    @GetMapping("/supplies/{supplyId}/trend")
    public Result<ConsumptionTrendDTO> getConsumptionTrend(
            @PathVariable Long supplyId,
            @RequestParam(defaultValue = "MONTH") String period) {
        return Result.success(inventoryWarningService.getConsumptionTrend(supplyId, period));
    }

    @PostMapping("/recalculate")
    public Result<List<SupplyWarningDTO>> recalculateWarnings() {
        return Result.success(inventoryWarningService.calculateAllWarnings());
    }

    @GetMapping("/messages")
    public Result<List<WarningMessageDTO>> getWarningMessages(
            @RequestParam(required = false) Integer readFlag,
            @RequestParam(required = false) String warningLevel) {
        User user = UserContext.getCurrentUser();
        return Result.success(warningNotificationService.getWarningMessages(user.getId(), readFlag, warningLevel));
    }

    @GetMapping("/messages/unread")
    public Result<List<WarningMessageDTO>> getUnreadMessages() {
        User user = UserContext.getCurrentUser();
        return Result.success(warningNotificationService.getUnreadMessages(user.getId()));
    }

    @GetMapping("/messages/unread-statistics")
    public Result<Map<String, Object>> getUnreadStatistics() {
        User user = UserContext.getCurrentUser();
        return Result.success(warningNotificationService.getUnreadStatistics(user.getId()));
    }

    @PostMapping("/messages/{id}/read")
    public Result<Void> markMessageAsRead(@PathVariable Long id) {
        boolean result = warningNotificationService.markAsRead(id);
        return result ? Result.successMsg("标记已读成功") : Result.error("标记失败");
    }

    @PostMapping("/messages/read-all")
    public Result<Void> markAllMessagesAsRead() {
        User user = UserContext.getCurrentUser();
        boolean result = warningNotificationService.markAllAsRead(user.getId());
        return result ? Result.successMsg("全部标记已读成功") : Result.error("标记失败");
    }

    @GetMapping("/purchase-suggestions")
    public Result<List<PurchaseSuggestionDTO>> getPurchaseSuggestions() {
        return Result.success(purchaseSuggestionService.generatePurchaseSuggestions());
    }

    @GetMapping("/purchase-suggestions/statistics")
    public Result<Map<String, Object>> getPurchaseSuggestionStatistics() {
        return Result.success(purchaseSuggestionService.getPurchaseSuggestionStatistics());
    }

    @GetMapping("/purchase-suggestions/{supplyId}")
    public Result<PurchaseSuggestionDTO> getSupplyPurchaseSuggestion(@PathVariable Long supplyId) {
        return Result.success(purchaseSuggestionService.getSupplyPurchaseSuggestion(supplyId));
    }

    @PostMapping("/purchase-suggestions/generate-purchase")
    public Result<Purchase> generatePurchaseFromSuggestions(
            @RequestBody List<PurchaseSuggestionDTO> suggestions,
            @RequestParam(required = false) String remark) {
        try {
            Purchase result = purchaseSuggestionService.createPurchaseFromSuggestions(suggestions, remark);
            return Result.success("采购单创建成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
