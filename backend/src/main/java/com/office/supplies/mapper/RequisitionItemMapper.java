package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.RequisitionItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RequisitionItemMapper extends BaseMapper<RequisitionItem> {

    @Select("SELECT i.*, s.supply_name, s.supply_code, s.unit, s.specification " +
            "FROM biz_requisition_item i " +
            "LEFT JOIN biz_supply s ON i.supply_id = s.id " +
            "WHERE i.requisition_id = #{requisitionId}")
    List<RequisitionItem> getItemsByRequisitionId(@Param("requisitionId") Long requisitionId);
}
