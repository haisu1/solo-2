package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.dto.DailyConsumptionDTO;
import com.office.supplies.entity.StockLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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

    @Select("<script>" +
            "SELECT " +
            "  FORMATDATETIME(l.create_time, 'yyyy-MM-dd') as dateStr, " +
            "  l.supply_id as supplyId, " +
            "  SUM(l.quantity) as consumption " +
            "FROM biz_stock_log l " +
            "WHERE l.operation_type IN ('OUT', 'ADJUST_OUT') " +
            "<if test='supplyId != null'>" +
            "AND l.supply_id = #{supplyId}" +
            "</if>" +
            "<if test='startDate != null and startDate != \"\"'>" +
            "AND l.create_time >= #{startDate}" +
            "</if>" +
            "<if test='endDate != null and endDate != \"\"'>" +
            "AND l.create_time &lt;= #{endDate}" +
            "</if>" +
            "GROUP BY FORMATDATETIME(l.create_time, 'yyyy-MM-dd'), l.supply_id " +
            "ORDER BY dateStr ASC" +
            "</script>")
    List<DailyConsumptionDTO> getDailyConsumption(@Param("supplyId") Long supplyId,
                                                  @Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    @Select("<script>" +
            "SELECT " +
            "  l.supply_id as supplyId, " +
            "  SUM(l.quantity) as totalConsumption " +
            "FROM biz_stock_log l " +
            "WHERE l.operation_type IN ('OUT', 'ADJUST_OUT') " +
            "<if test='supplyId != null'>" +
            "AND l.supply_id = #{supplyId}" +
            "</if>" +
            "<if test='days != null'>" +
            "AND l.create_time >= DATEADD('DAY', -#{days}, CURRENT_TIMESTAMP)" +
            "</if>" +
            "GROUP BY l.supply_id" +
            "</script>")
    List<Map<String, Object>> getTotalConsumptionByDays(@Param("supplyId") Long supplyId,
                                                        @Param("days") Integer days);

    @Select("<script>" +
            "SELECT " +
            "  FORMATDATETIME(l.create_time, 'yyyy-MM') as period, " +
            "  l.supply_id as supplyId, " +
            "  SUM(l.quantity) as consumption " +
            "FROM biz_stock_log l " +
            "WHERE l.operation_type IN ('OUT', 'ADJUST_OUT') " +
            "<if test='supplyId != null'>" +
            "AND l.supply_id = #{supplyId}" +
            "</if>" +
            "<if test='months != null'>" +
            "AND l.create_time >= DATEADD('MONTH', -#{months}, CURRENT_TIMESTAMP)" +
            "</if>" +
            "GROUP BY FORMATDATETIME(l.create_time, 'yyyy-MM'), l.supply_id " +
            "ORDER BY period ASC" +
            "</script>")
    List<Map<String, Object>> getMonthlyConsumption(@Param("supplyId") Long supplyId,
                                                    @Param("months") Integer months);

    @Select("<script>" +
            "SELECT " +
            "  CONCAT(YEAR(l.create_time), '-Q', QUARTER(l.create_time)) as period, " +
            "  l.supply_id as supplyId, " +
            "  SUM(l.quantity) as consumption " +
            "FROM biz_stock_log l " +
            "WHERE l.operation_type IN ('OUT', 'ADJUST_OUT') " +
            "<if test='supplyId != null'>" +
            "AND l.supply_id = #{supplyId}" +
            "</if>" +
            "<if test='quarters != null'>" +
            "AND l.create_time >= DATEADD('MONTH', -#{quarters}*3, CURRENT_TIMESTAMP)" +
            "</if>" +
            "GROUP BY YEAR(l.create_time), QUARTER(l.create_time), l.supply_id " +
            "ORDER BY period ASC" +
            "</script>")
    List<Map<String, Object>> getQuarterlyConsumption(@Param("supplyId") Long supplyId,
                                                      @Param("quarters") Integer quarters);
}
