package com.office.supplies.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PurchaseSuggestionDTO implements Serializable {

    private Long supplyId;

    private String supplyName;

    private String supplyCode;

    private Long categoryId;

    private String categoryName;

    private String unit;

    private String specification;

    private BigDecimal price;

    private Integer currentStock;

    private Integer inTransitQuantity;

    private Integer safeStockQuantity;

    private Double dailyConsumption;

    private Integer predictedConsumption15Days;

    private Integer predictedConsumption30Days;

    private Integer supplierLeadTime;

    private Integer eoqQuantity;

    private Integer suggestedQuantity;

    private String warningLevel;

    private BigDecimal estimatedAmount;

    private String supplierName;

    private LocalDateTime suggestTime;
}
