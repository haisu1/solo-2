package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.supplies.entity.InventoryCheck;
import com.office.supplies.query.InventoryCheckQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryCheckMapper extends BaseMapper<InventoryCheck> {

    @Select("<script>" +
            "SELECT c.*, u.real_name as checked_by_name " +
            "FROM biz_inventory_check c " +
            "LEFT JOIN sys_user u ON c.checked_by = u.id " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND c.check_no LIKE CONCAT('%',#{keyword},'%')" +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND c.status = #{status}" +
            "</if>" +
            "</where>" +
            "ORDER BY c.id DESC" +
            "</script>")
    List<InventoryCheck> getCheckList(@Param("keyword") String keyword,
                                      @Param("status") String status);

    @Select("<script>" +
            "SELECT c.*, u.real_name as checked_by_name " +
            "FROM biz_inventory_check c " +
            "LEFT JOIN sys_user u ON c.checked_by = u.id " +
            "<where>" +
            "<if test='query.keyword != null and query.keyword != \"\"'>" +
            "AND c.check_no LIKE CONCAT('%',#{query.keyword},'%')" +
            "</if>" +
            "<if test='query.status != null and query.status != \"\"'>" +
            "AND c.status = #{query.status}" +
            "</if>" +
            "</where>" +
            "ORDER BY c.id DESC" +
            "</script>")
    IPage<InventoryCheck> selectInventoryCheckPage(Page<InventoryCheck> page, @Param("query") InventoryCheckQuery query);

    @Select("SELECT c.*, u.real_name as checked_by_name " +
            "FROM biz_inventory_check c " +
            "LEFT JOIN sys_user u ON c.checked_by = u.id " +
            "WHERE c.id = #{id}")
    InventoryCheck getCheckDetail(@Param("id") Long id);
}
