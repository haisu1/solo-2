package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.StockLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StockLogMapper extends BaseMapper<StockLog> {

    @Select("<script>" +
            "SELECT l.*, s.supply_name, u.real_name as operator_name " +
            "FROM biz_stock_log l " +
            "LEFT JOIN biz_supply s ON l.supply_id = s.id " +
            "LEFT JOIN sys_user u ON l.operator_id = u.id " +
            "<where>" +
            "<if test='supplyId != null'>" +
            "AND l.supply_id = #{supplyId}" +
            "</if>" +
            "<if test='operationType != null and operationType != \"\"'>" +
            "AND l.operation_type = #{operationType}" +
            "</if>" +
            "</where>" +
            "ORDER BY l.id DESC LIMIT 200" +
            "</script>")
    List<StockLog> getStockLogList(@Param("supplyId") Long supplyId,
                                   @Param("operationType") String operationType);
}
