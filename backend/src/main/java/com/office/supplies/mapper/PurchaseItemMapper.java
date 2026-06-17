package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.PurchaseItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchaseItemMapper extends BaseMapper<PurchaseItem> {

    @Select("SELECT i.*, s.supply_name, s.supply_code, s.unit " +
            "FROM biz_purchase_item i " +
            "LEFT JOIN biz_supply s ON i.supply_id = s.id " +
            "WHERE i.purchase_id = #{purchaseId}")
    List<PurchaseItem> getItemsByPurchaseId(@Param("purchaseId") Long purchaseId);
}
