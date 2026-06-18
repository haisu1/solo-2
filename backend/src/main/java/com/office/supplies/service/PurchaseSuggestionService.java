package com.office.supplies.service;

import com.office.supplies.common.WarningLevel;
import com.office.supplies.dto.PurchaseSuggestionDTO;
import com.office.supplies.dto.SupplyWarningDTO;
import com.office.supplies.entity.Purchase;
import com.office.supplies.entity.PurchaseItem;
import com.office.supplies.entity.Supply;
import com.office.supplies.mapper.PurchaseMapper;
import com.office.supplies.mapper.SupplyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseSuggestionService.class);

    private static final int DEFAULT_LEAD_TIME_DAYS = 7;
    private static final double ORDERING_COST = 100.0;
    private static final double HOLDING_COST_RATE = 0.2;
    private static final int FORECAST_DAYS = 30;

    @Resource
    private InventoryWarningService inventoryWarningService;

    @Resource
    private SupplyMapper supplyMapper;

    @Resource
    private PurchaseMapper purchaseMapper;

    @Resource
    private PurchaseService purchaseService;

    public List<PurchaseSuggestionDTO> generatePurchaseSuggestions() {
        List<PurchaseSuggestionDTO> suggestions = new ArrayList<>();

        List<SupplyWarningDTO> warnings = inventoryWarningService.getAllWarnings(null);
        Map<Long, Integer> inTransitMap = getInTransitQuantityMap();

        for (SupplyWarningDTO warning : warnings) {
            if (WarningLevel.NORMAL.getCode().equals(warning.getWarningLevel())) {
                continue;
            }

            PurchaseSuggestionDTO suggestion = calculateSuggestion(warning, inTransitMap);
            if (suggestion != null && suggestion.getSuggestedQuantity() > 0) {
                suggestions.add(suggestion);
            }
        }

        suggestions.sort((a, b) -> {
            int levelCompare = WarningLevel.valueOf(b.getWarningLevel()).getPriority()
                    - WarningLevel.valueOf(a.getWarningLevel()).getPriority();
            if (levelCompare != 0) {
                return levelCompare;
            }
            return b.getSuggestedQuantity().compareTo(a.getSuggestedQuantity());
        });

        logger.info("采购建议生成完成，共 {} 条建议", suggestions.size());
        return suggestions;
    }

    public PurchaseSuggestionDTO getSupplyPurchaseSuggestion(Long supplyId) {
        SupplyWarningDTO warning = inventoryWarningService.getSupplyWarning(supplyId);
        if (warning == null) {
            return null;
        }
        Map<Long, Integer> inTransitMap = getInTransitQuantityMap();
        return calculateSuggestion(warning, inTransitMap);
    }

    private PurchaseSuggestionDTO calculateSuggestion(SupplyWarningDTO warning, Map<Long, Integer> inTransitMap) {
        PurchaseSuggestionDTO suggestion = new PurchaseSuggestionDTO();

        suggestion.setSupplyId(warning.getId());
        suggestion.setSupplyName(warning.getSupplyName());
        suggestion.setSupplyCode(warning.getSupplyCode());
        suggestion.setCategoryId(warning.getCategoryId());
        suggestion.setCategoryName(warning.getCategoryName());
        suggestion.setUnit(warning.getUnit());
        suggestion.setSpecification(warning.getSpecification());
        suggestion.setPrice(warning.getPrice());
        suggestion.setWarningLevel(warning.getWarningLevel());
        suggestion.setSuggestTime(LocalDateTime.now());

        int currentStock = warning.getStock() != null ? warning.getStock() : 0;
        suggestion.setCurrentStock(currentStock);

        int inTransit = inTransitMap.getOrDefault(warning.getId(), 0);
        suggestion.setInTransitQuantity(inTransit);

        double dailyConsumption = warning.getDailyConsumption() != null ? warning.getDailyConsumption() : 0.0;
        suggestion.setDailyConsumption(dailyConsumption);

        int safeStock = warning.getSafeStockQuantity() != null ? warning.getSafeStockQuantity() : 0;
        suggestion.setSafeStockQuantity(safeStock);

        int predicted15 = (int) Math.ceil(dailyConsumption * 15);
        int predicted30 = (int) Math.ceil(dailyConsumption * 30);
        suggestion.setPredictedConsumption15Days(predicted15);
        suggestion.setPredictedConsumption30Days(predicted30);

        int leadTime = DEFAULT_LEAD_TIME_DAYS;
        suggestion.setSupplierLeadTime(leadTime);

        int leadTimeConsumption = (int) Math.ceil(dailyConsumption * leadTime);

        int eoq = calculateEOQ(dailyConsumption, warning.getPrice());
        suggestion.setEoqQuantity(eoq);

        int totalAvailable = currentStock + inTransit;
        int requiredStock = safeStock + predicted30;

        int suggestedQuantity = 0;
        if (totalAvailable < requiredStock) {
            suggestedQuantity = requiredStock - totalAvailable;
            suggestedQuantity = Math.max(suggestedQuantity, eoq / 2);
            suggestedQuantity = Math.max(suggestedQuantity, (int) Math.ceil(dailyConsumption * FORECAST_DAYS));
        }

        if (suggestedQuantity > 0 && warning.getMaxStock() != null && warning.getMaxStock() > 0) {
            int maxToOrder = warning.getMaxStock() - totalAvailable;
            if (maxToOrder > 0) {
                suggestedQuantity = Math.min(suggestedQuantity, maxToOrder);
            }
        }

        suggestion.setSuggestedQuantity(suggestedQuantity);

        if (suggestedQuantity > 0 && warning.getPrice() != null) {
            BigDecimal estimated = warning.getPrice().multiply(new BigDecimal(suggestedQuantity));
            suggestion.setEstimatedAmount(estimated.setScale(2, RoundingMode.HALF_UP));
        } else {
            suggestion.setEstimatedAmount(BigDecimal.ZERO);
        }

        return suggestion;
    }

    private int calculateEOQ(double dailyConsumption, BigDecimal unitPrice) {
        if (dailyConsumption <= 0 || unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        double annualDemand = dailyConsumption * 365;
        double holdingCostPerUnit = unitPrice.doubleValue() * HOLDING_COST_RATE;

        if (holdingCostPerUnit <= 0) {
            return (int) Math.ceil(annualDemand / 12);
        }

        double eoq = Math.sqrt((2 * annualDemand * ORDERING_COST) / holdingCostPerUnit);
        return (int) Math.ceil(eoq);
    }

    private Map<Long, Integer> getInTransitQuantityMap() {
        Map<Long, Integer> result = new HashMap<>();
        List<Map<String, Object>> inTransitData = purchaseMapper.getInTransitQuantity(null);
        for (Map<String, Object> row : inTransitData) {
            Long supplyId = ((Number) row.get("supplyId")).longValue();
            int quantity = ((Number) row.get("inTransitQuantity")).intValue();
            result.put(supplyId, quantity);
        }
        return result;
    }

    public Purchase createPurchaseFromSuggestions(List<PurchaseSuggestionDTO> suggestions, String remark) {
        if (suggestions == null || suggestions.isEmpty()) {
            throw new RuntimeException("采购建议不能为空");
        }

        Purchase purchase = new Purchase();
        purchase.setRemark(remark != null ? remark : "系统自动生成采购建议单");
        purchase.setSupplierName("系统推荐供应商");

        List<PurchaseItem> items = new ArrayList<>();
        for (PurchaseSuggestionDTO suggestion : suggestions) {
            if (suggestion.getSuggestedQuantity() == null || suggestion.getSuggestedQuantity() <= 0) {
                continue;
            }
            PurchaseItem item = new PurchaseItem();
            item.setSupplyId(suggestion.getSupplyId());
            item.setQuantity(suggestion.getSuggestedQuantity());
            item.setUnitPrice(suggestion.getPrice() != null ? suggestion.getPrice() : BigDecimal.ZERO);
            if (suggestion.getPrice() != null) {
                item.setTotalPrice(suggestion.getPrice().multiply(new BigDecimal(suggestion.getSuggestedQuantity())));
            } else {
                item.setTotalPrice(BigDecimal.ZERO);
            }
            items.add(item);
        }

        if (items.isEmpty()) {
            throw new RuntimeException("没有有效的采购明细");
        }

        purchase.setItems(items);

        Purchase result = purchaseService.createPurchase(purchase);
        logger.info("根据采购建议创建采购单成功，采购单号：{}，共 {} 项物资", result.getPurchaseNo(), items.size());
        return result;
    }

    public Map<String, Object> getPurchaseSuggestionStatistics() {
        Map<String, Object> result = new HashMap<>();
        List<PurchaseSuggestionDTO> suggestions = generatePurchaseSuggestions();

        int urgentCount = 0, warningCount = 0, attentionCount = 0;
        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PurchaseSuggestionDTO s : suggestions) {
            switch (WarningLevel.valueOf(s.getWarningLevel())) {
                case URGENT:
                    urgentCount++;
                    break;
                case WARNING:
                    warningCount++;
                    break;
                case ATTENTION:
                    attentionCount++;
                    break;
            }
            if (s.getSuggestedQuantity() != null) {
                totalQuantity += s.getSuggestedQuantity();
            }
            if (s.getEstimatedAmount() != null) {
                totalAmount = totalAmount.add(s.getEstimatedAmount());
            }
        }

        result.put("totalSuggestions", suggestions.size());
        result.put("urgentCount", urgentCount);
        result.put("warningCount", warningCount);
        result.put("attentionCount", attentionCount);
        result.put("totalQuantity", totalQuantity);
        result.put("totalAmount", totalAmount.setScale(2, RoundingMode.HALF_UP));

        return result;
    }
}
