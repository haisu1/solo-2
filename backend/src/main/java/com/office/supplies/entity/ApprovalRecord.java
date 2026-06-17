package com.office.supplies.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Duration;

@Data
@TableName("biz_approval_record")
public class ApprovalRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String bizType;

    private Long bizId;

    private String bizNo;

    private Long flowId;

    private Integer currentLevel;

    private Integer totalLevels;

    private String nodeName;

    private Long approverId;

    private String approverName;

    private String action;

    private String remark;

    private LocalDateTime startTime;

    private LocalDateTime approveTime;

    private Long transferFromId;

    private String transferFromName;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private Long durationMinutes;

    @TableField(exist = false)
    private String durationText;

    @TableField(exist = false)
    private Object bizData;

    public void calculateDuration() {
        if (startTime != null && approveTime != null) {
            Duration d = Duration.between(startTime, approveTime);
            this.durationMinutes = d.toMinutes();
            long hours = d.toHours();
            long mins = d.toMinutesPart();
            if (hours > 0) {
                this.durationText = hours + "小时" + mins + "分钟";
            } else {
                this.durationText = mins + "分钟";
            }
        }
    }
}
