package com.office.supplies.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_warning_message")
public class WarningMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long supplyId;

    private String supplyName;

    private String supplyCode;

    private String warningLevel;

    private String warningContent;

    private Integer currentStock;

    private Integer safeStockQuantity;

    private Integer stockDays;

    private Long userId;

    private Integer readFlag;

    private LocalDateTime readTime;

    private LocalDateTime createTime;
}
