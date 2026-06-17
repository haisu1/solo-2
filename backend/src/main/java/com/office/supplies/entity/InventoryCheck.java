package com.office.supplies.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("biz_inventory_check")
public class InventoryCheck implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String checkNo;

    private String checkType;

    private String status;

    private Long checkedBy;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String checkedByName;

    @TableField(exist = false)
    private List<InventoryCheckItem> items;
}
