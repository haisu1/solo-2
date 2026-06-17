package com.office.supplies.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_department")
public class Department implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String deptName;

    private String deptCode;

    private Long parentId;

    private String leader;

    private String phone;

    private String description;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
