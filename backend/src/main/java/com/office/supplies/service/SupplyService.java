package com.office.supplies.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.common.UserContext;
import com.office.supplies.entity.StockLog;
import com.office.supplies.entity.Supply;
import com.office.supplies.mapper.StockLogMapper;
import com.office.supplies.mapper.SupplyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SupplyService extends ServiceImpl<SupplyMapper, Supply> {

    @Resource
    private SupplyMapper supplyMapper;

    @Resource
    private StockLogMapper stockLogMapper;

    public PageResult<Supply> getSupplyPage(PageQuery query, Long categoryId, Integer status, Boolean lowStock) {
        List<Supply> list = supplyMapper.getSupplyList(query.getKeyword(), categoryId, status, lowStock);
        long total = list.size();
        long start = (query.getCurrent() - 1) * query.getSize();
        long end = Math.min(start + query.getSize(), total);
        List<Supply> records = list.subList((int) start, (int) end);
        PageResult<Supply> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        result.setCurrent(query.getCurrent());
        result.setSize(query.getSize());
        return result;
    }

    public List<Supply> getSupplyList(Long categoryId, String keyword) {
        return supplyMapper.getSupplyList(keyword, categoryId, 1, false);
    }

    public Supply getSupplyDetail(Long id) {
        return supplyMapper.selectById(id);
    }

    public List<Supply> getLowStockSupplies() {
        return supplyMapper.getSupplyList(null, null, 1, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addStock(Long supplyId, Integer quantity, String relatedNo, String remark) {
        Supply supply = this.getById(supplyId);
        if (supply == null) {
            throw new RuntimeException("物资不存在");
        }
        int beforeStock = supply.getStock();
        supplyMapper.addStock(supplyId, quantity);
        int afterStock = beforeStock + quantity;
        StockLog log = new StockLog();
        log.setSupplyId(supplyId);
        log.setOperationType("IN");
        log.setQuantity(quantity);
        log.setBeforeStock(beforeStock);
        log.setAfterStock(afterStock);
        log.setRelatedNo(relatedNo);
        log.setOperatorId(UserContext.getCurrentUserId());
        log.setRemark(remark);
        stockLogMapper.insert(log);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reduceStock(Long supplyId, Integer quantity, String relatedNo, String remark) {
        Supply supply = this.getById(supplyId);
        if (supply == null) {
            throw new RuntimeException("物资不存在");
        }
        if (supply.getStock() < quantity) {
            throw new RuntimeException("库存不足，物资：" + supply.getSupplyName() + "，当前库存：" + supply.getStock());
        }
        int beforeStock = supply.getStock();
        int rows = supplyMapper.reduceStock(supplyId, quantity);
        if (rows == 0) {
            throw new RuntimeException("扣减库存失败");
        }
        int afterStock = beforeStock - quantity;
        StockLog log = new StockLog();
        log.setSupplyId(supplyId);
        log.setOperationType("OUT");
        log.setQuantity(quantity);
        log.setBeforeStock(beforeStock);
        log.setAfterStock(afterStock);
        log.setRelatedNo(relatedNo);
        log.setOperatorId(UserContext.getCurrentUserId());
        log.setRemark(remark);
        stockLogMapper.insert(log);
    }
}
