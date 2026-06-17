package com.office.supplies.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.entity.StockLog;
import com.office.supplies.mapper.StockLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StockLogService extends ServiceImpl<StockLogMapper, StockLog> {

    @Resource
    private StockLogMapper stockLogMapper;

    public List<StockLog> getStockLogList(Long supplyId, String operationType) {
        return stockLogMapper.getStockLogList(supplyId, operationType);
    }
}
