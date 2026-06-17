package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.ApprovalFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ApprovalFlowMapper extends BaseMapper<ApprovalFlow> {

    @Select("SELECT f.*, d.dept_name as departmentName, c.category_name as categoryName " +
            "FROM biz_approval_flow f " +
            "LEFT JOIN sys_department d ON f.department_id = d.id " +
            "LEFT JOIN biz_category c ON f.category_id = c.id " +
            "WHERE f.biz_type = #{bizType} " +
            "AND f.status = 1 " +
            "AND (f.department_id IS NULL OR f.department_id = #{departmentId}) " +
            "AND (f.category_id IS NULL OR f.category_id = #{categoryId}) " +
            "AND f.min_amount <= #{amount} AND f.max_amount >= #{amount} " +
            "ORDER BY f.priority DESC, f.id DESC")
    List<ApprovalFlow> findMatchingFlows(@Param("bizType") String bizType,
                                         @Param("departmentId") Long departmentId,
                                         @Param("categoryId") Long categoryId,
                                         @Param("amount") BigDecimal amount);

    @Select("SELECT f.*, d.dept_name as departmentName, c.category_name as categoryName " +
            "FROM biz_approval_flow f " +
            "LEFT JOIN sys_department d ON f.department_id = d.id " +
            "LEFT JOIN biz_category c ON f.category_id = c.id " +
            "WHERE (#{bizType} IS NULL OR f.biz_type = #{bizType}) " +
            "AND (#{keyword} IS NULL OR f.flow_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY f.id DESC")
    List<ApprovalFlow> getFlowList(@Param("bizType") String bizType,
                                   @Param("keyword") String keyword);
}
