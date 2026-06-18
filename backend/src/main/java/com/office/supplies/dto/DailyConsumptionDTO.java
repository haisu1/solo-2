package com.office.supplies.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DailyConsumptionDTO implements Serializable {

    private String dateStr;

    private Long supplyId;

    private Integer consumption;
}
