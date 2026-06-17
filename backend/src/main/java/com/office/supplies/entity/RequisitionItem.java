package com.office.supplies.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("biz_requisition_item")
public class RequisitionItem implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long requisitionId;

    private Long supplyId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    @TableField(exist = false)
    private String supplyName;

    @TableField(exist = false)
    private String supplyCode;

    @TableField(exist = false)
    private String unit;

    @TableField(exist = false)
    private String specification;
}
