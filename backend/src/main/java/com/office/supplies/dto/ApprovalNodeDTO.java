package com.office.supplies.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApprovalNodeDTO implements Serializable {
    private Long id;
    private Long flowId;
    private Integer nodeLevel;
    private String nodeName;
    private String approverType;
    private String approverIds;
    private String roleCode;
    private Integer timeoutHours;
    private Integer canTransfer;
    private Integer canWithdraw;
}
