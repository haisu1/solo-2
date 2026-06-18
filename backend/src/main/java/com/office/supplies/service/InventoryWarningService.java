package com.office.supplies.service;

import com.office.supplies.common.WarningLevel;
import com.office.supplies.dto.ConsumptionTrendDTO;
import com.office.supplies.dto.DailyConsumptionDTO;
import com.office.supplies.dto.SupplyWarningDTO;
import com.office.supplies.entity.Supply;
import com.office.supplies.mapper.StockLogMapper;
import com.office.supplies.mapper.SupplyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class InventoryWarningService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryWarningService.class);

    private static final int DEFAULT_SAFE_STOCK_DAYS = 15;
    private static final int CONSUMPTION_CALCULATION_DAYS = 30;
    private static final int MIN_HISTORY_DAYS = 7;

    private final Map<Long, SupplyWarningDTO> warningCache = new ConcurrentHashMap<>();
    private volatile LocalDate lastCalculationDate = null;

    @Resource
    private SupplyMapper supplyMapper;

    @Resource
    private StockLogMapper stockLogMapper;

    public List<SupplyWarningDTO> calculateAllWarnings() {
        List<Supply> supplies = supplyMapper.getSupplyList(null, null, 1, false);
        List<SupplyWarningDTO> warnings = new ArrayList<>();

        Map<Long, Double> dailyConsumptionMap = calculateDailyConsumptionForAll();

        for (Supply supply : supplies) {
            SupplyWarningDTO warning = calculateSupplyWarning(supply, dailyConsumptionMap);
            warnings.add(warning);
            warningCache.put(supply.getId(), warning);
        }

        lastCalculationDate = LocalDate.now();
        logger.info("库存预警计算完成，共处理 {} 个物资，其中预警物资 {} 个",
                warnings.size(),
                warnings.stream().filter(w -> !WarningLevel.NORMAL.getCode().equals(w.getWarningLevel())).count());

        return warnings;
    }

    public SupplyWarningDTO calculateSupplyWarning(Supply supply, Map<Long, Double> dailyConsumptionMap) {
        SupplyWarningDTO warning = new SupplyWarningDTO();
        warning.setId(supply.getId());
        warning.setSupplyName(supply.getSupplyName());
        warning.setSupplyCode(supply.getSupplyCode());
        warning.setCategoryId(supply.getCategoryId());
        warning.setCategoryName(supply.getCategoryName());
        warning.setUnit(supply.getUnit());
        warning.setSpecification(supply.getSpecification());
        warning.setPrice(supply.getPrice());
        warning.setStock(supply.getStock());
        warning.setMinStock(supply.getMinStock());
        warning.setMaxStock(supply.getMaxStock());
        warning.setCreateTime(supply.getCreateTime());
        warning.setUpdateTime(supply.getUpdateTime());

        double dailyConsumption = dailyConsumptionMap.getOrDefault(supply.getId(), 0.0);
        warning.setDailyConsumption(roundToTwoDecimals(dailyConsumption));

        int safeStockDays = calculateSafeStockDays(supply, dailyConsumption);
        warning.setSafeStockDays(safeStockDays);

        int safeStockQuantity = (int) Math.ceil(dailyConsumption * safeStockDays);
        safeStockQuantity = Math.max(safeStockQuantity, supply.getMinStock() != null ? supply.getMinStock() : 0);
        warning.setSafeStockQuantity(safeStockQuantity);

        int stockDays = calculateStockDays(supply.getStock(), dailyConsumption);
        warning.setStockDays(stockDays);

        warning.setPredictedStockIn7Days(predictFutureStock(supply.getStock(), dailyConsumption, 7));
        warning.setPredictedStockIn15Days(predictFutureStock(supply.getStock(), dailyConsumption, 15));
        warning.setPredictedStockIn30Days(predictFutureStock(supply.getStock(), dailyConsumption, 30));

        WarningLevel level = determineWarningLevel(supply, stockDays, safeStockDays, dailyConsumption);
        warning.setWarningLevel(level.getCode());
        warning.setWarningLevelDesc(level.getDesc());

        return warning;
    }

    private Map<Long, Double> calculateDailyConsumptionForAll() {
        Map<Long, Double> result = new HashMap<>();

        List<Map<String, Object>> consumptionData = stockLogMapper.getTotalConsumptionByDays(null, CONSUMPTION_CALCULATION_DAYS);

        for (Map<String, Object> row : consumptionData) {
            Long supplyId = ((Number) row.get("supplyId")).longValue();
            int totalConsumption = ((Number) row.get("totalConsumption")).intValue();
            double dailyConsumption = (double) totalConsumption / CONSUMPTION_CALCULATION_DAYS;
            result.put(supplyId, dailyConsumption);
        }

        return result;
    }

    public double calculateDailyConsumption(Long supplyId) {
        List<Map<String, Object>> data = stockLogMapper.getTotalConsumptionByDays(supplyId, CONSUMPTION_CALCULATION_DAYS);
        if (data == null || data.isEmpty()) {
            return 0.0;
        }
        int totalConsumption = ((Number) data.get(0).get("totalConsumption")).intValue();
        return (double) totalConsumption / CONSUMPTION_CALCULATION_DAYS;
    }

    public int calculateSafeStockDays(Supply supply, double dailyConsumption) {
        int baseDays = DEFAULT_SAFE_STOCK_DAYS;

        if (dailyConsumption > 0) {
            double cv = calculateConsumptionVolatility(supply.getId());
            if (cv > 0.5) {
                baseDays = 30;
            } else if (cv > 0.3) {
                baseDays = 20;
            } else if (cv > 0.1) {
                baseDays = DEFAULT_SAFE_STOCK_DAYS;
            } else {
                baseDays = 10;
            }
        }

        if (supply.getMinStock() != null && supply.getMinStock() > 0 && dailyConsumption > 0) {
            int minStockDays = (int) Math.ceil(supply.getMinStock() / dailyConsumption);
            baseDays = Math.max(baseDays, minStockDays);
        }

        return baseDays;
    }

    private double calculateConsumptionVolatility(Long supplyId) {
        List<DailyConsumptionDTO> dailyData = stockLogMapper.getDailyConsumption(supplyId, null, null);
        if (dailyData == null || dailyData.size() < MIN_HISTORY_DAYS) {
            return 0.0;
        }

        List<Integer> consumptions = dailyData.stream()
                .map(DailyConsumptionDTO::getConsumption)
                .filter(c -> c != null && c > 0)
                .collect(Collectors.toList());

        if (consumptions.size() < MIN_HISTORY_DAYS) {
            return 0.0;
        }

        double mean = consumptions.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        if (mean == 0) {
            return 0.0;
        }

        double variance = consumptions.stream()
                .mapToDouble(c -> Math.pow(c - mean, 2))
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(variance);
        return stdDev / mean;
    }

    public int calculateStockDays(Integer currentStock, double dailyConsumption) {
        if (currentStock == null || currentStock <= 0) {
            return 0;
        }
        if (dailyConsumption <= 0) {
            return 999;
        }
        return (int) Math.floor(currentStock / dailyConsumption);
    }

    public int predictFutureStock(Integer currentStock, double dailyConsumption, int days) {
        if (currentStock == null) {
            return 0;
        }
        int predicted = (int) Math.floor(currentStock - dailyConsumption * days);
        return Math.max(predicted, 0);
    }

    private WarningLevel determineWarningLevel(Supply supply, int stockDays, int safeStockDays, double dailyConsumption) {
        int stock = supply.getStock() != null ? supply.getStock() : 0;
        int minStock = supply.getMinStock() != null ? supply.getMinStock() : 0;

        if (dailyConsumption <= 0) {
            if (stock <= minStock && minStock > 0) {
                return WarningLevel.WARNING;
            }
            return WarningLevel.NORMAL;
        }

        WarningLevel levelByDays = WarningLevel.getByStockDays(stockDays, safeStockDays);

        if (stock <= 0) {
            return WarningLevel.URGENT;
        }

        if (stock <= minStock) {
            if (levelByDays.getPriority() > WarningLevel.WARNING.getPriority()) {
                return levelByDays;
            }
            return WarningLevel.WARNING;
        }

        return levelByDays;
    }

    public List<SupplyWarningDTO> getAllWarnings(String warningLevel) {
        List<SupplyWarningDTO> warnings;
        if (lastCalculationDate == null || !LocalDate.now().equals(lastCalculationDate)) {
            warnings = calculateAllWarnings();
        } else {
            warnings = new ArrayList<>(warningCache.values());
        }

        if (warningLevel != null && !warningLevel.isEmpty()) {
            warnings = warnings.stream()
                    .filter(w -> warningLevel.equals(w.getWarningLevel()))
                    .collect(Collectors.toList());
        }

        warnings.sort((a, b) -> {
            int levelCompare = WarningLevel.valueOf(b.getWarningLevel()).getPriority()
                    - WarningLevel.valueOf(a.getWarningLevel()).getPriority();
            if (levelCompare != 0) {
                return levelCompare;
            }
            return Integer.compare(a.getStockDays(), b.getStockDays());
        });

        return warnings;
    }

    public List<SupplyWarningDTO> getWarningSupplies() {
        return getAllWarnings(null).stream()
                .filter(w -> !WarningLevel.NORMAL.getCode().equals(w.getWarningLevel()))
                .collect(Collectors.toList());
    }

    public SupplyWarningDTO getSupplyWarning(Long supplyId) {
        if (warningCache.containsKey(supplyId) && lastCalculationDate != null && LocalDate.now().equals(lastCalculationDate)) {
            return warningCache.get(supplyId);
        }
        Supply supply = supplyMapper.selectById(supplyId);
        if (supply == null) {
            return null;
        }
        Map<Long, Double> consumptionMap = new HashMap<>();
        consumptionMap.put(supplyId, calculateDailyConsumption(supplyId));
        SupplyWarningDTO warning = calculateSupplyWarning(supply, consumptionMap);
        warningCache.put(supplyId, warning);
        return warning;
    }

    public ConsumptionTrendDTO getConsumptionTrend(Long supplyId, String periodType) {
        Supply supply = supplyMapper.selectById(supplyId);
        if (supply == null) {
            return null;
        }

        ConsumptionTrendDTO trend = new ConsumptionTrendDTO();
        trend.setSupplyId(supplyId);
        trend.setSupplyName(supply.getSupplyName());
        trend.setSupplyCode(supply.getSupplyCode());
        trend.setPeriod(periodType);

        List<String> labels = new ArrayList<>();
        List<Integer> data = new ArrayList<>();
        Map<String, Integer> periodData = new LinkedHashMap<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        if ("WEEK".equalsIgnoreCase(periodType)) {
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                String label = date.format(dateFormatter);
                labels.add(label);
                periodData.put(label, 0);
            }
            List<DailyConsumptionDTO> dailyData = stockLogMapper.getDailyConsumption(
                    supplyId, today.minusDays(6).atStartOfDay().toString(), today.atTime(23, 59, 59).toString());
            fillDailyData(periodData, dailyData);

        } else if ("MONTH".equalsIgnoreCase(periodType)) {
            for (int i = 29; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                String label = date.format(dateFormatter);
                labels.add(label);
                periodData.put(label, 0);
            }
            List<DailyConsumptionDTO> dailyData = stockLogMapper.getDailyConsumption(
                    supplyId, today.minusDays(29).atStartOfDay().toString(), today.atTime(23, 59, 59).toString());
            fillDailyData(periodData, dailyData);

        } else if ("QUARTER".equalsIgnoreCase(periodType)) {
            List<Map<String, Object>> monthlyData = stockLogMapper.getMonthlyConsumption(supplyId, 3);
            for (Map<String, Object> row : monthlyData) {
                String period = (String) row.get("period");
                int consumption = ((Number) row.get("consumption")).intValue();
                labels.add(period);
                data.add(consumption);
                periodData.put(period, consumption);
            }
        } else {
            List<Map<String, Object>> monthlyData = stockLogMapper.getMonthlyConsumption(supplyId, 6);
            for (Map<String, Object> row : monthlyData) {
                String period = (String) row.get("period");
                int consumption = ((Number) row.get("consumption")).intValue();
                labels.add(period);
                data.add(consumption);
                periodData.put(period, consumption);
            }
        }

        if ("WEEK".equalsIgnoreCase(periodType) || "MONTH".equalsIgnoreCase(periodType)) {
            data = new ArrayList<>(periodData.values());
        }

        trend.setDateLabels(labels);
        trend.setConsumptionData(data);
        trend.setPeriodData(periodData);

        int totalConsumption = data.stream().mapToInt(Integer::intValue).sum();
        trend.setTotalConsumption(totalConsumption);

        double avgDaily = data.isEmpty() ? 0.0 : (double) totalConsumption / data.size();
        trend.setAvgDailyConsumption(roundToTwoDecimals(avgDaily));

        trend.setTrendRate(calculateTrendRate(data));

        return trend;
    }

    private void fillDailyData(Map<String, Integer> periodData, List<DailyConsumptionDTO> dailyData) {
        if (dailyData != null) {
            for (DailyConsumptionDTO dto : dailyData) {
                if (dto.getDateStr() != null && periodData.containsKey(dto.getDateStr())) {
                    periodData.put(dto.getDateStr(), dto.getConsumption() != null ? dto.getConsumption() : 0);
                }
            }
        }
    }

    private Double calculateTrendRate(List<Integer> data) {
        if (data == null || data.size() < 4) {
            return 0.0;
        }

        int halfSize = data.size() / 2;
        List<Integer> firstHalf = data.subList(0, halfSize);
        List<Integer> secondHalf = data.subList(halfSize, data.size());

        double firstAvg = firstHalf.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double secondAvg = secondHalf.stream().mapToInt(Integer::intValue).average().orElse(0.0);

        if (firstAvg == 0) {
            return secondAvg > 0 ? 1.0 : 0.0;
        }

        return roundToTwoDecimals((secondAvg - firstAvg) / firstAvg);
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public Map<String, Object> getWarningStatistics() {
        Map<String, Object> result = new HashMap<>();
        List<SupplyWarningDTO> allWarnings = getAllWarnings(null);

        int normalCount = 0, attentionCount = 0, warningCount = 0, urgentCount = 0;

        for (SupplyWarningDTO w : allWarnings) {
            switch (WarningLevel.valueOf(w.getWarningLevel())) {
                case NORMAL:
                    normalCount++;
                    break;
                case ATTENTION:
                    attentionCount++;
                    break;
                case WARNING:
                    warningCount++;
                    break;
                case URGENT:
                    urgentCount++;
                    break;
            }
        }

        result.put("totalCount", allWarnings.size());
        result.put("normalCount", normalCount);
        result.put("attentionCount", attentionCount);
        result.put("warningCount", warningCount);
        result.put("urgentCount", urgentCount);
        result.put("warningTotalCount", attentionCount + warningCount + urgentCount);

        return result;
    }
}
