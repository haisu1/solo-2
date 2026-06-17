package com.office.supplies.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ApprovalFlowDTO implements Serializable {
    private Long id;
    private String flowName;
    private String bizType;
    private Long departmentId;
    private Long categoryId;
    private java.math.BigDecimal minAmount;
    private java.math.BigDecimal maxAmount;
    private Integer priority;
    private Integer status;
    private String nodeConfig;
    private List<ApprovalNodeDTO> nodes;
}
