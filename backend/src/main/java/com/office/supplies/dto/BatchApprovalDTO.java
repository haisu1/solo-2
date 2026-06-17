package com.office.supplies.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BatchApprovalDTO implements Serializable {
    private String bizType;
    private List<Long> bizIds;
    private String action;
    private String remark;
    private Long transferToUserId;
}
