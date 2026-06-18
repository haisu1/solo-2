package com.office.supplies.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("biz_sequence")
public class Sequence implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String seqName;

    private String seqDate;

    private Integer currentValue;

    private Integer maxValue;

    @Version
    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
