package com.office.supplies.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("biz_inventory_check_item")
public class InventoryCheckItem implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long checkId;

    private Long supplyId;

    private Integer systemStock;

    private Integer actualStock;

    private Integer diffQuantity;

    private String diffReason;

    @TableField(exist = false)
    private String supplyName;

    @TableField(exist = false)
    private String supplyCode;

    @TableField(exist = false)
    private String unit;
}
