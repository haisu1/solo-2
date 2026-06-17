package com.office.supplies.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_supply")
public class Supply implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String supplyName;

    private String supplyCode;

    private Long categoryId;

    private String unit;

    private String specification;

    private BigDecimal price;

    private Integer stock;

    private Integer minStock;

    private Integer maxStock;

    private String imageUrl;

    private String description;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String categoryName;
}
