package com.office.supplies.service;

import com.office.supplies.common.UserContext;
import com.office.supplies.entity.Supply;
import com.office.supplies.entity.User;
import com.office.supplies.mapper.RequisitionMapper;
import com.office.supplies.mapper.SupplyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

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
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        long totalSupplyCount = supplyMapper.countTotal();
        long lowStockCount = supplyMapper.countLowStock();
        long totalStock = supplyMapper.sumStock();
        double totalValue = supplyMapper.sumStockValue();
        long pendingRequisitionCount = requisitionMapper.countPending();
        long totalRequisitionCount = requisitionMapper.countTotal();

        result.put("totalSupplyCount", (int) totalSupplyCount);
        result.put("lowStockCount", (int) lowStockCount);
        result.put("totalStock", (int) totalStock);
        result.put("totalValue", Math.round(totalValue * 100) / 100.0);
        result.put("pendingRequisitionCount", (int) pendingRequisitionCount);
        result.put("totalRequisitionCount", (int) totalRequisitionCount);

        List<Supply> lowStockList = supplyMapper.getSupplyList(null, null, 1, true);
        result.put("lowStockList", lowStockList);

        result.put("warningStatistics", inventoryWarningService.getWarningStatistics());
        result.put("warningSupplyList", inventoryWarningService.getWarningSupplies());

        try {
            User user = UserContext.getCurrentUser();
            if (user != null) {
                result.put("unreadWarningCount", warningNotificationService.getUnreadStatistics(user.getId()));
            }
        } catch (Exception e) {
            logger.debug("Get unread warning count failed: {}", e.getMessage());
        }

        result.put("purchaseSuggestionStatistics", purchaseSuggestionService.getPurchaseSuggestionStatistics());

        long costTime = System.currentTimeMillis() - startTime;
        if (costTime > 1000) {
            logger.warn("Slow query detected - getDashboardData cost: {}ms", costTime);
        }
        return result;
    }

    public Map<String, Object> getRequisitionStatistics(String startDate, String endDate) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        List<Map<String, Object>> statusCounts = requisitionMapper.countByStatusAndDateRange(startDate, endDate);

        Map<String, Long> statusStats = new HashMap<>();
        statusStats.put("PENDING", 0L);
        statusStats.put("APPROVED", 0L);
        statusStats.put("REJECTED", 0L);
        statusStats.put("CANCELLED", 0L);

        long totalCount = 0;
        for (Map<String, Object> entry : statusCounts) {
            String status = entry.get("statusKey") != null ? entry.get("statusKey").toString() : "";
            long count = entry.get("countValue") != null ? ((Number) entry.get("countValue")).longValue() : 0;
            totalCount += count;

            if (status.startsWith("PENDING")) {
                statusStats.merge("PENDING", count, Long::sum);
            } else if (status.equals("APPROVED")) {
                statusStats.put("APPROVED", count);
            } else if (status.equals("REJECTED")) {
                statusStats.put("REJECTED", count);
            } else if (status.equals("CANCELLED")) {
                statusStats.put("CANCELLED", count);
            }
        }

        result.put("statusStats", statusStats);
        result.put("totalCount", totalCount);

        long costTime = System.currentTimeMillis() - startTime;
        if (costTime > 1000) {
            logger.warn("Slow query detected - getRequisitionStatistics cost: {}ms, startDate: {}, endDate: {}",
                    costTime, startDate, endDate);
        }
        return result;
    }
}
