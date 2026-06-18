package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.supplies.entity.Supply;
import com.office.supplies.query.SupplyQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SupplyMapper extends BaseMapper<Supply> {

    @Select("<script>" +
            "SELECT s.*, c.category_name FROM biz_supply s " +
            "LEFT JOIN biz_category c ON s.category_id = c.id " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (s.supply_name LIKE CONCAT('%',#{keyword},'%') OR s.supply_code LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "<if test='categoryId != null'>" +
            "AND s.category_id = #{categoryId}" +
            "</if>" +
            "<if test='status != null'>" +
            "AND s.status = #{status}" +
            "</if>" +
            "<if test='lowStock != null and lowStock == true'>" +
            "AND s.stock &lt;= s.min_stock" +
            "</if>" +
            "</where>" +
            "ORDER BY s.id DESC" +
            "</script>")
    List<Supply> getSupplyList(@Param("keyword") String keyword,
                               @Param("categoryId") Long categoryId,
                               @Param("status") Integer status,
                               @Param("lowStock") Boolean lowStock);

    @Select("<script>" +
            "SELECT s.*, c.category_name FROM biz_supply s " +
            "LEFT JOIN biz_category c ON s.category_id = c.id " +
            "<where>" +
            "<if test='query.keyword != null and query.keyword != \"\"'>" +
            "AND (s.supply_name LIKE CONCAT('%',#{query.keyword},'%') OR s.supply_code LIKE CONCAT('%',#{query.keyword},'%'))" +
            "</if>" +
            "<if test='query.categoryId != null'>" +
            "AND s.category_id = #{query.categoryId}" +
            "</if>" +
            "<if test='query.status != null'>" +
            "AND s.status = #{query.status}" +
            "</if>" +
            "<if test='query.lowStock != null and query.lowStock == true'>" +
            "AND s.stock &lt;= s.min_stock" +
            "</if>" +
            "</where>" +
            "ORDER BY s.id DESC" +
            "</script>")
    IPage<Supply> selectSupplyPage(Page<Supply> page, @Param("query") SupplyQuery query);

    @Select("SELECT COUNT(*) FROM biz_supply")
    long countTotal();

    @Select("SELECT COUNT(*) FROM biz_supply WHERE stock &lt;= min_stock")
    long countLowStock();

    @Select("SELECT COALESCE(SUM(stock), 0) FROM biz_supply")
    long sumStock();

    @Select("SELECT COALESCE(SUM(stock * price), 0) FROM biz_supply")
    double sumStockValue();

    @Select("SELECT category_id as categoryId, COUNT(*) as countValue FROM biz_supply GROUP BY category_id")
    List<Map<String, Object>> countByCategory();

    @Select("SELECT * FROM biz_supply WHERE id = #{supplyId} FOR UPDATE")
    Supply selectByIdForUpdate(@Param("supplyId") Long supplyId);

    @Update("UPDATE biz_supply SET stock = stock + #{quantity}, update_time = CURRENT_TIMESTAMP WHERE id = #{supplyId} AND version = #{version}")
    int addStock(@Param("supplyId") Long supplyId, @Param("quantity") Integer quantity, @Param("version") Integer version);

    @Update("UPDATE biz_supply SET stock = stock - #{quantity}, update_time = CURRENT_TIMESTAMP WHERE id = #{supplyId} AND stock >= #{quantity} AND version = #{version}")
    int reduceStock(@Param("supplyId") Long supplyId, @Param("quantity") Integer quantity, @Param("version") Integer version);
}
