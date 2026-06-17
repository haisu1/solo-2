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
@TableName("biz_purchase")
public class Purchase implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String purchaseNo;

    private Long supplierId;

    private String supplierName;

    private BigDecimal totalAmount;

    private String status;

    private Long createdBy;

    private Long approvedBy;

    private LocalDateTime approveTime;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String createdByName;

    @TableField(exist = false)
    private String approvedByName;

    @TableField(exist = false)
    private List<PurchaseItem> items;
}
