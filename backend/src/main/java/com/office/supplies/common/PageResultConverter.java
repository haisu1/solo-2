package com.office.supplies.common;

import com.baomidou.mybatisplus.core.metadata.IPage;

public class PageResultConverter {

    public static <T> PageResult<T> convert(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    public static <T> PageResult<T> convert(IPage<T> page, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords());
        result.setCurrent(current);
        result.setSize(size);
        return result;
    }
}
