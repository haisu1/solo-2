package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.supplies.entity.Purchase;
import com.office.supplies.query.PurchaseQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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

    @Select("<script>" +
            "SELECT p.*, u.real_name as created_by_name, a.real_name as approved_by_name " +
            "FROM biz_purchase p " +
            "LEFT JOIN sys_user u ON p.created_by = u.id " +
            "LEFT JOIN sys_user a ON p.approved_by = a.id " +
            "<where>" +
            "<if test='query.keyword != null and query.keyword != \"\"'>" +
            "AND (p.purchase_no LIKE CONCAT('%',#{query.keyword},'%') OR p.supplier_name LIKE CONCAT('%',#{query.keyword},'%'))" +
            "</if>" +
            "<if test='query.status != null and query.status != \"\"'>" +
            "AND p.status = #{query.status}" +
            "</if>" +
            "</where>" +
            "ORDER BY p.id DESC" +
            "</script>")
    IPage<Purchase> selectPurchasePage(Page<Purchase> page, @Param("query") PurchaseQuery query);

    @Select("SELECT COUNT(*) FROM biz_purchase WHERE status LIKE 'PENDING%'")
    long countPending();

    @Select("SELECT status as statusKey, COUNT(*) as countValue FROM biz_purchase GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT p.*, u.real_name as created_by_name, a.real_name as approved_by_name " +
            "FROM biz_purchase p " +
            "LEFT JOIN sys_user u ON p.created_by = u.id " +
            "LEFT JOIN sys_user a ON p.approved_by = a.id " +
            "WHERE p.id = #{id}")
    Purchase getPurchaseDetail(@Param("id") Long id);

    @Select("<script>" +
            "SELECT " +
            "  pi.supply_id as supplyId, " +
            "  SUM(pi.quantity) as inTransitQuantity " +
            "FROM biz_purchase p " +
            "INNER JOIN biz_purchase_item pi ON p.id = pi.purchase_id " +
            "WHERE p.status IN ('APPROVED', 'PENDING_LEVEL1', 'PENDING_LEVEL2', 'PENDING_LEVEL3') " +
            "<if test='supplyId != null'>" +
            "AND pi.supply_id = #{supplyId}" +
            "</if>" +
            "GROUP BY pi.supply_id" +
            "</script>")
    List<Map<String, Object>> getInTransitQuantity(@Param("supplyId") Long supplyId);

    @Select("SELECT p.*, u.real_name as created_by_name, a.real_name as approved_by_name " +
            "FROM biz_purchase p " +
            "LEFT JOIN sys_user u ON p.created_by = u.id " +
            "LEFT JOIN sys_user a ON p.approved_by = a.id " +
            "WHERE p.status IN ('APPROVED', 'PENDING_LEVEL1', 'PENDING_LEVEL2', 'PENDING_LEVEL3') " +
            "ORDER BY p.create_time DESC")
    List<Purchase> getPendingAndApprovedPurchases();
}
