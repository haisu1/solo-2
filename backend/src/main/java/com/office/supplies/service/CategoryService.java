package com.office.supplies.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.common.PageResultConverter;
import com.office.supplies.entity.Category;
import com.office.supplies.mapper.CategoryMapper;
import com.office.supplies.query.CategoryQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Resource
    private CategoryMapper categoryMapper;

    public List<Category> getAllCategories() {
        return categoryMapper.getCategoryList(null);
    }

    @Deprecated
    public PageResult<Category> getCategoryPage(PageQuery query) {
        CategoryQuery categoryQuery = CategoryQuery.builder()
                .current(query.getCurrent())
                .size(query.getSize())
                .keyword(query.getKeyword())
                .build();
        return getCategoryPage(categoryQuery);
    }

    public PageResult<Category> getCategoryPage(CategoryQuery query) {
        long startTime = System.currentTimeMillis();
        Page<Category> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<Category> resultPage = categoryMapper.selectCategoryPage(page, query);
        long costTime = System.currentTimeMillis() - startTime;
        if (costTime > 1000) {
            logger.warn("Slow query detected - getCategoryPage cost: {}ms, params: {}", costTime, query);
        }
        return PageResultConverter.convert(resultPage, query.getCurrent(), query.getSize());
    }
}
