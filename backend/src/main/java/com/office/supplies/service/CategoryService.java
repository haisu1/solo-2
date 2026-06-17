package com.office.supplies.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.entity.Category;
import com.office.supplies.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CategoryService extends ServiceImpl<CategoryMapper, Category> {

    @Resource
    private CategoryMapper categoryMapper;

    public List<Category> getAllCategories() {
        return categoryMapper.getCategoryList(null);
    }

    public PageResult<Category> getCategoryPage(PageQuery query) {
        List<Category> list = categoryMapper.getCategoryList(query.getKeyword());
        long total = list.size();
        long start = (query.getCurrent() - 1) * query.getSize();
        long end = Math.min(start + query.getSize(), total);
        List<Category> records = list.subList((int) start, (int) end);
        PageResult<Category> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        result.setCurrent(query.getCurrent());
        result.setSize(query.getSize());
        return result;
    }
}
