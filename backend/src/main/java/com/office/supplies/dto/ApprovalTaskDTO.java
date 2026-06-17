package com.office.supplies.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ApprovalTaskDTO implements Serializable {
    private Long id;
    private String bizType;
    private Long bizId;
    private String bizNo;
    private Integer currentLevel;
    private Integer totalLevels;
    private String nodeName;
    private String approverId;
    private String approverName;
    private String status;
    private String applicantName;
    private String departmentName;
    private java.math.BigDecimal totalAmount;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime startTime;
    private Long timeoutMinutes;
    private Boolean isUrgent;
    private Object bizData;
    private List<com.office.supplies.entity.ApprovalRecord> historyRecords;
}
