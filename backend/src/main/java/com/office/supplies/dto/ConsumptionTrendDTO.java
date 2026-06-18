package com.office.supplies.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ConsumptionTrendDTO implements Serializable {

    private Long supplyId;

    private String supplyName;

    private String supplyCode;

    private String period;

    private List<String> dateLabels;

    private List<Integer> consumptionData;

    private Double avgDailyConsumption;

    private Double trendRate;

    private Integer totalConsumption;

    private Map<String, Integer> periodData;
}
