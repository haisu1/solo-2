package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.office.supplies.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("<script>" +
            "SELECT c.*, (SELECT COUNT(*) FROM biz_supply s WHERE s.category_id = c.id) as supply_count " +
            "FROM biz_category c " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (c.category_name LIKE CONCAT('%',#{keyword},'%') OR c.category_code LIKE CONCAT('%',#{keyword},'%'))" +
            "</if>" +
            "</where>" +
            "ORDER BY c.sort_order ASC, c.id ASC" +
            "</script>")
    List<Category> getCategoryList(@Param("keyword") String keyword);
}
