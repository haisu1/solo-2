package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.Requisition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    @Select("SELECT r.*, u.username, u.real_name, d.dept_name as department_name, a.real_name as approved_by_name " +
            "FROM biz_requisition r " +
            "LEFT JOIN sys_user u ON r.user_id = u.id " +
            "LEFT JOIN sys_department d ON r.department_id = d.id " +
            "LEFT JOIN sys_user a ON r.approved_by = a.id " +
            "WHERE r.id = #{id}")
    Requisition getRequisitionDetail(@Param("id") Long id);
}
