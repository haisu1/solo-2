package com.office.supplies.service;

import com.office.supplies.common.UserContext;
import com.office.supplies.entity.Requisition;
import com.office.supplies.entity.Supply;
import com.office.supplies.entity.User;
import com.office.supplies.mapper.RequisitionMapper;
import com.office.supplies.mapper.SupplyMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    @Resource
    private SupplyMapper supplyMapper;

    @Resource
    private RequisitionMapper requisitionMapper;

    @Resource
    private InventoryWarningService inventoryWarningService;

    @Resource
    private WarningNotificationService warningNotificationService;

    @Resource
    private PurchaseSuggestionService purchaseSuggestionService;

    public Map<String, Object> getDashboardData() {
        Map<String, Object> result = new HashMap<>();
        List<Supply> allSupplies = supplyMapper.getSupplyList(null, null, null, false);
        List<Supply> lowStock = supplyMapper.getSupplyList(null, null, 1, true);
        List<Requisition> pendingRequisitions = requisitionMapper.getRequisitionList(null, null, "PENDING", null);
        List<Requisition> allRequisitions = requisitionMapper.getRequisitionList(null, null, null, null);
        int totalStock = allSupplies.stream().mapToInt(s -> s.getStock() != null ? s.getStock() : 0).sum();
        double totalValue = allSupplies.stream()
                .mapToDouble(s -> (s.getStock() != null ? s.getStock() : 0) * (s.getPrice() != null ? s.getPrice().doubleValue() : 0))
                .sum();
        result.put("totalSupplyCount", allSupplies.size());
        result.put("lowStockCount", lowStock.size());
        result.put("totalStock", totalStock);
        result.put("totalValue", Math.round(totalValue * 100) / 100.0);
        result.put("pendingRequisitionCount", pendingRequisitions.size());
        result.put("totalRequisitionCount", allRequisitions.size());
        result.put("lowStockList", lowStock);

        result.put("warningStatistics", inventoryWarningService.getWarningStatistics());
        result.put("warningSupplyList", inventoryWarningService.getWarningSupplies());

        try {
            User user = UserContext.getCurrentUser();
            if (user != null) {
                result.put("unreadWarningCount", warningNotificationService.getUnreadStatistics(user.getId()));
            }
        } catch (Exception e) {
        }

        result.put("purchaseSuggestionStatistics", purchaseSuggestionService.getPurchaseSuggestionStatistics());

        return result;
    }

    public Map<String, Object> getRequisitionStatistics(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();
        List<Requisition> all = requisitionMapper.getRequisitionList(null, null, null, null);
        long pending = all.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long approved = all.stream().filter(r -> "APPROVED".equals(r.getStatus())).count();
        long rejected = all.stream().filter(r -> "REJECTED".equals(r.getStatus())).count();
        long cancelled = all.stream().filter(r -> "CANCELLED".equals(r.getStatus())).count();
        Map<String, Long> statusStats = new HashMap<>();
        statusStats.put("PENDING", pending);
        statusStats.put("APPROVED", approved);
        statusStats.put("REJECTED", rejected);
        statusStats.put("CANCELLED", cancelled);
        result.put("statusStats", statusStats);
        result.put("totalCount", all.size());
        return result;
    }
}
