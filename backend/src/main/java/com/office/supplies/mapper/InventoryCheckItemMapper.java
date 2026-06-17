package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.InventoryCheckItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryCheckItemMapper extends BaseMapper<InventoryCheckItem> {

    @Select("SELECT i.*, s.supply_name, s.supply_code, s.unit " +
            "FROM biz_inventory_check_item i " +
            "LEFT JOIN biz_supply s ON i.supply_id = s.id " +
            "WHERE i.check_id = #{checkId}")
    List<InventoryCheckItem> getItemsByCheckId(@Param("checkId") Long checkId);
}
