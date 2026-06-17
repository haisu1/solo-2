package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.ApprovalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    @Select("SELECT r.* FROM biz_approval_record r " +
            "WHERE r.biz_type = #{bizType} AND r.biz_id = #{bizId} " +
            "ORDER BY r.current_level ASC, r.create_time ASC")
    List<ApprovalRecord> getRecordsByBiz(@Param("bizType") String bizType,
                                         @Param("bizId") Long bizId);

    @Select("SELECT r.* FROM biz_approval_record r " +
            "WHERE r.approver_id = #{approverId} " +
            "AND r.action IS NULL " +
            "AND r.biz_type = #{bizType} " +
            "ORDER BY r.create_time DESC")
    List<ApprovalRecord> getPendingTasksByApprover(@Param("approverId") Long approverId,
                                                   @Param("bizType") String bizType);

    @Select("SELECT r.* FROM biz_approval_record r " +
            "WHERE r.approver_id = #{approverId} " +
            "AND r.action IS NULL " +
            "ORDER BY r.create_time DESC")
    List<ApprovalRecord> getAllPendingTasksByApprover(@Param("approverId") Long approverId);

    @Select("SELECT r.* FROM biz_approval_record r " +
            "WHERE r.action IS NULL " +
            "AND r.start_time IS NOT NULL " +
            "AND r.start_time <= #{timeoutTime}")
    List<ApprovalRecord> getTimeoutTasks(@Param("timeoutTime") LocalDateTime timeoutTime);

    @Select("SELECT * FROM biz_approval_record r " +
            "WHERE r.biz_type = #{bizType} AND r.biz_id = #{bizId} " +
            "AND r.current_level = #{level} " +
            "ORDER BY r.id DESC LIMIT 1")
    ApprovalRecord getLatestRecordByBizAndLevel(@Param("bizType") String bizType,
                                                @Param("bizId") Long bizId,
                                                @Param("level") Integer level);
}
