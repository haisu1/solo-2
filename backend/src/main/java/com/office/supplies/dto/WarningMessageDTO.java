package com.office.supplies.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class WarningMessageDTO implements Serializable {

    private Long id;

    private Long supplyId;

    private String supplyName;

    private String supplyCode;

    private String warningLevel;

    private String warningLevelDesc;

    private String warningContent;

    private Integer currentStock;

    private Integer safeStockQuantity;

    private Integer stockDays;

    private Boolean readFlag;

    private LocalDateTime readTime;

    private LocalDateTime createTime;
}
