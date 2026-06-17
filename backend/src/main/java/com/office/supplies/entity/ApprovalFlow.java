package com.office.supplies.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("biz_approval_flow")
public class ApprovalFlow implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String flowName;

    private String bizType;

    private Long departmentId;

    private Long categoryId;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private Integer priority;

    private Integer status;

    private String nodeConfig;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private List<ApprovalNode> nodes;
}
