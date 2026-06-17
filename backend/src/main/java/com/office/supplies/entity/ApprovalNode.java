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
@TableName("biz_approval_node")
public class ApprovalNode implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long flowId;

    private Integer nodeLevel;

    private String nodeName;

    private String approverType;

    private String approverIds;

    private String roleCode;

    private Integer timeoutHours;

    private Integer canTransfer;

    private Integer canWithdraw;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<User> approvers;

    @TableField(exist = false)
    private String roleName;
}
