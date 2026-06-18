package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.supplies.entity.Requisition;
import com.office.supplies.query.RequisitionQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface RequisitionMapper extends BaseMapper<Requisition> {

    @Select("<script>" +
            "SELECT r.*, u.username, u.real_name, d.dept_name as department_name, a.real_name as approved_by_name " +
            "FROM biz_requisition r " +
            "LEFT JOIN sys_user u ON r.user_id = u.id " +
            "LEFT JOIN sys_department d ON r.department_id = d.id " +
            "LEFT JOIN sys_user a ON r.approved_by = a.id " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (r.requisition_no LIKE CONCAT('%',#{keyword},'%') OR u.real_name LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "<if test='userId != null'>" +
            "AND r.user_id = #{userId}" +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND r.status = #{status}" +
            "</if>" +
            "<if test='departmentId != null'>" +
            "AND r.department_id = #{departmentId}" +
            "</if>" +
            "</where>" +
            "ORDER BY r.id DESC" +
            "</script>")
    List<Requisition> getRequisitionList(@Param("keyword") String keyword,
                                         @Param("userId") Long userId,
                                         @Param("status") String status,
                                         @Param("departmentId") Long departmentId);

    @Select("<script>" +
            "SELECT r.*, u.username, u.real_name, d.dept_name as department_name, a.real_name as approved_by_name " +
            "FROM biz_requisition r " +
            "LEFT JOIN sys_user u ON r.user_id = u.id " +
            "LEFT JOIN sys_department d ON r.department_id = d.id " +
            "LEFT JOIN sys_user a ON r.approved_by = a.id " +
            "<where>" +
            "<if test='query.keyword != null and query.keyword != \"\"'>" +
            "AND (r.requisition_no LIKE CONCAT('%',#{query.keyword},'%') OR u.real_name LIKE CONCAT('%',#{query.keyword},'%'))" +
            "</if>" +
            "<if test='query.userId != null'>" +
            "AND r.user_id = #{query.userId}" +
            "</if>" +
            "<if test='query.status != null and query.status != \"\"'>" +
            "AND r.status = #{query.status}" +
            "</if>" +
            "<if test='query.departmentId != null'>" +
            "AND r.department_id = #{query.departmentId}" +
            "</if>" +
            "<if test='query.startDate != null and query.startDate != \"\"'>" +
            "AND r.create_time &gt;= #{query.startDate}" +
            "</if>" +
            "<if test='query.endDate != null and query.endDate != \"\"'>" +
            "AND r.create_time &lt;= CONCAT(#{query.endDate}, ' 23:59:59')" +
            "</if>" +
            "</where>" +
            "ORDER BY r.id DESC" +
            "</script>")
    IPage<Requisition> selectRequisitionPage(Page<Requisition> page, @Param("query") RequisitionQuery query);

    @Select("SELECT status as statusKey, COUNT(*) as countValue FROM biz_requisition GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("<script>" +
            "SELECT status as statusKey, COUNT(*) as countValue FROM biz_requisition " +
            "<where>" +
            "<if test='startDate != null and startDate != \"\"'>" +
            "AND create_time &gt;= #{startDate}" +
            "</if>" +
            "<if test='endDate != null and endDate != \"\"'>" +
            "AND create_time &lt;= CONCAT(#{endDate}, ' 23:59:59')" +
            "</if>" +
            "</where>" +
            "GROUP BY status" +
            "</script>")
    List<Map<String, Object>> countByStatusAndDateRange(@Param("startDate") String startDate,
                                                        @Param("endDate") String endDate);

    @Select("SELECT COUNT(*) FROM biz_requisition WHERE status LIKE 'PENDING%'")
    long countPending();

    @Select("SELECT COUNT(*) FROM biz_requisition")
    long countTotal();

    @Select("SELECT r.*, u.username, u.real_name, d.dept_name as department_name, a.real_name as approved_by_name " +
            "FROM biz_requisition r " +
            "LEFT JOIN sys_user u ON r.user_id = u.id " +
            "LEFT JOIN sys_department d ON r.department_id = d.id " +
            "LEFT JOIN sys_user a ON r.approved_by = a.id " +
            "WHERE r.id = #{id}")
    Requisition getRequisitionDetail(@Param("id") Long id);
}
