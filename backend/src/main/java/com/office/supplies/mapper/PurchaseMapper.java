package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.Purchase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchaseMapper extends BaseMapper<Purchase> {

    @Select("<script>" +
            "SELECT p.*, u.real_name as created_by_name, a.real_name as approved_by_name " +
            "FROM biz_purchase p " +
            "LEFT JOIN sys_user u ON p.created_by = u.id " +
            "LEFT JOIN sys_user a ON p.approved_by = a.id " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (p.purchase_no LIKE CONCAT('%',#{keyword},'%') OR p.supplier_name LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND p.status = #{status}" +
            "</if>" +
            "</where>" +
            "ORDER BY p.id DESC" +
            "</script>")
    List<Purchase> getPurchaseList(@Param("keyword") String keyword,
                                   @Param("status") String status);

    @Select("SELECT p.*, u.real_name as created_by_name, a.real_name as approved_by_name " +
            "FROM biz_purchase p " +
            "LEFT JOIN sys_user u ON p.created_by = u.id " +
            "LEFT JOIN sys_user a ON p.approved_by = a.id " +
            "WHERE p.id = #{id}")
    Purchase getPurchaseDetail(@Param("id") Long id);
}
