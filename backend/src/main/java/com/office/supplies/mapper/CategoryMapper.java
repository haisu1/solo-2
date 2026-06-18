package com.office.supplies.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.office.supplies.entity.Category;
import com.office.supplies.query.CategoryQuery;
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

    @Select("<script>" +
            "SELECT c.*, (SELECT COUNT(*) FROM biz_supply s WHERE s.category_id = c.id) as supply_count " +
            "FROM biz_category c " +
            "<where>" +
            "<if test='query.keyword != null and query.keyword != \"\"'>" +
            "AND (c.category_name LIKE CONCAT('%',#{query.keyword},'%') OR c.category_code LIKE CONCAT('%',#{query.keyword},'%'))" +
            "</if>" +
            "<if test='query.parentId != null'>" +
            "AND c.parent_id = #{query.parentId}" +
            "</if>" +
            "</where>" +
            "ORDER BY c.sort_order ASC, c.id ASC" +
            "</script>")
    IPage<Category> selectCategoryPage(Page<Category> page, @Param("query") CategoryQuery query);
}
