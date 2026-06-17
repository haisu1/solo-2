package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.Supply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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

    @Update("UPDATE biz_supply SET stock = stock + #{quantity}, update_time = CURRENT_TIMESTAMP WHERE id = #{supplyId}")
    int addStock(@Param("supplyId") Long supplyId, @Param("quantity") Integer quantity);

    @Update("UPDATE biz_supply SET stock = stock - #{quantity}, update_time = CURRENT_TIMESTAMP WHERE id = #{supplyId} AND stock >= #{quantity}")
    int reduceStock(@Param("supplyId") Long supplyId, @Param("quantity") Integer quantity);
}
