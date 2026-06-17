package com.office.supplies.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_stock_log")
public class StockLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long supplyId;

    private String operationType;

    private Integer quantity;

    private Integer beforeStock;

    private Integer afterStock;

    private String relatedNo;

    private Long operatorId;

    private String remark;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String supplyName;

    @TableField(exist = false)
    private String operatorName;
}
