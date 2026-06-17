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
@TableName("biz_requisition")
public class Requisition implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String requisitionNo;

    private Long userId;

    private Long departmentId;

    private String purpose;

    private String remark;

    private String status;

    private Long approvedBy;

    private LocalDateTime approveTime;

    private String approveRemark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String realName;

    @TableField(exist = false)
    private String departmentName;

    @TableField(exist = false)
    private String approvedByName;

    @TableField(exist = false)
    private List<RequisitionItem> items;
}
