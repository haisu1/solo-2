package com.office.supplies.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SupplyWarningDTO implements Serializable {

    private Long id;

    private String supplyName;

    private String supplyCode;

    private Long categoryId;

    private String categoryName;

    private String unit;

    private String specification;

    private BigDecimal price;

    private Integer stock;

    private Integer minStock;

    private Integer maxStock;

    private String warningLevel;

    private String warningLevelDesc;

    private Double dailyConsumption;

    private Integer safeStockDays;

    private Integer safeStockQuantity;

    private Integer stockDays;

    private Integer predictedStockIn7Days;

    private Integer predictedStockIn15Days;

    private Integer predictedStockIn30Days;

    private LocalDateTime lastWarningTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
