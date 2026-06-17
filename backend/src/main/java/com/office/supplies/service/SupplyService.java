package com.office.supplies.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.entity.Supply;
import com.office.supplies.mapper.SupplyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SupplyService extends ServiceImpl<SupplyMapper, Supply> {

    private static final Logger logger = LoggerFactory.getLogger(SupplyService.class);

    @Resource
    private SupplyMapper supplyMapper;

    @Resource
    private StockLogService stockLogService;

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

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean checkStockAvailability(Map<Long, Integer> stockRequirements) {
        List<String> errors = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : stockRequirements.entrySet()) {
            Long supplyId = entry.getKey();
            Integer quantity = entry.getValue();
            Supply supply = supplyMapper.selectByIdForUpdate(supplyId);
            if (supply == null) {
                errors.add("物资ID：" + supplyId + " 不存在");
                continue;
            }
            if (supply.getStock() < quantity) {
                errors.add("物资：" + supply.getSupplyName() + " 库存不足，当前库存：" + supply.getStock() + "，需要：" + quantity);
            }
        }
        if (!errors.isEmpty()) {
            throw new RuntimeException(String.join("；", errors));
        }
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void batchReduceStock(Map<Long, Integer> stockRequirements, String relatedNo, String remark) {
        checkStockAvailability(stockRequirements);
        for (Map.Entry<Long, Integer> entry : stockRequirements.entrySet()) {
            Long supplyId = entry.getKey();
            Integer quantity = entry.getValue();
            reduceStockInternal(supplyId, quantity, relatedNo, remark);
        }
        validateStockLogConsistency(stockRequirements.keySet(), relatedNo);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void addStock(Long supplyId, Integer quantity, String relatedNo, String remark) {
        Supply supply = supplyMapper.selectByIdForUpdate(supplyId);
        if (supply == null) {
            throw new RuntimeException("物资不存在");
        }
        int beforeStock = supply.getStock();
        int rows = supplyMapper.addStock(supplyId, quantity, supply.getVersion());
        if (rows == 0) {
            logger.warn("乐观锁冲突，重试中... supplyId: {}, version: {}", supplyId, supply.getVersion());
            supply = supplyMapper.selectByIdForUpdate(supplyId);
            rows = supplyMapper.addStock(supplyId, quantity, supply.getVersion());
            if (rows == 0) {
                throw new RuntimeException("增加库存失败，乐观锁冲突，请重试");
            }
        }
        int afterStock = beforeStock + quantity;
        stockLogService.recordStockLog(supplyId, "IN", quantity, beforeStock, afterStock, relatedNo, remark);
        stockLogService.validateStockLogIntegrity(supplyId);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void reduceStock(Long supplyId, Integer quantity, String relatedNo, String remark) {
        Map<Long, Integer> requirements = new HashMap<>();
        requirements.put(supplyId, quantity);
        batchReduceStock(requirements, relatedNo, remark);
    }

    private void reduceStockInternal(Long supplyId, Integer quantity, String relatedNo, String remark) {
        Supply supply = supplyMapper.selectByIdForUpdate(supplyId);
        if (supply == null) {
            throw new RuntimeException("物资不存在");
        }
        if (supply.getStock() < quantity) {
            throw new RuntimeException("库存不足，物资：" + supply.getSupplyName() + "，当前库存：" + supply.getStock() + "，需要：" + quantity);
        }
        int beforeStock = supply.getStock();
        int rows = supplyMapper.reduceStock(supplyId, quantity, supply.getVersion());
        if (rows == 0) {
            logger.warn("乐观锁冲突，重试中... supplyId: {}, version: {}", supplyId, supply.getVersion());
            supply = supplyMapper.selectByIdForUpdate(supplyId);
            if (supply.getStock() < quantity) {
                throw new RuntimeException("库存不足，物资：" + supply.getSupplyName() + "，当前库存：" + supply.getStock() + "，需要：" + quantity);
            }
            rows = supplyMapper.reduceStock(supplyId, quantity, supply.getVersion());
            if (rows == 0) {
                throw new RuntimeException("扣减库存失败，乐观锁冲突，请重试");
            }
        }
        int afterStock = beforeStock - quantity;
        stockLogService.recordStockLog(supplyId, "OUT", quantity, beforeStock, afterStock, relatedNo, remark);
        logger.info("库存扣减成功 - supplyId: {}, supplyName: {}, before: {}, quantity: {}, after: {}, relatedNo: {}",
                supplyId, supply.getSupplyName(), beforeStock, quantity, afterStock, relatedNo);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void adjustStock(Long supplyId, Integer diffQuantity, String relatedNo, String remark) {
        Supply supply = supplyMapper.selectByIdForUpdate(supplyId);
        if (supply == null) {
            throw new RuntimeException("物资不存在");
        }
        int beforeStock = supply.getStock();
        int afterStock = beforeStock + diffQuantity;
        if (afterStock < 0) {
            throw new RuntimeException("调整后库存不能为负数，物资：" + supply.getSupplyName());
        }
        String operationType = diffQuantity >= 0 ? "ADJUST_IN" : "ADJUST_OUT";
        int adjustQuantity = Math.abs(diffQuantity);
        int rows;
        if (diffQuantity >= 0) {
            rows = supplyMapper.addStock(supplyId, adjustQuantity, supply.getVersion());
        } else {
            rows = supplyMapper.reduceStock(supplyId, adjustQuantity, supply.getVersion());
        }
        if (rows == 0) {
            logger.warn("乐观锁冲突，重试中... supplyId: {}, version: {}", supplyId, supply.getVersion());
            supply = supplyMapper.selectByIdForUpdate(supplyId);
            if (diffQuantity < 0 && supply.getStock() < adjustQuantity) {
                throw new RuntimeException("库存不足，物资：" + supply.getSupplyName() + "，当前库存：" + supply.getStock());
            }
            if (diffQuantity >= 0) {
                rows = supplyMapper.addStock(supplyId, adjustQuantity, supply.getVersion());
            } else {
                rows = supplyMapper.reduceStock(supplyId, adjustQuantity, supply.getVersion());
            }
            if (rows == 0) {
                throw new RuntimeException("调整库存失败，乐观锁冲突，请重试");
            }
        }
        stockLogService.recordStockLog(supplyId, operationType, adjustQuantity, beforeStock, afterStock, relatedNo, remark);
        logger.info("库存调整成功 - supplyId: {}, supplyName: {}, before: {}, diff: {}, after: {}, relatedNo: {}",
                supplyId, supply.getSupplyName(), beforeStock, diffQuantity, afterStock, relatedNo);
        stockLogService.validateStockLogIntegrity(supplyId);
    }

    private void validateStockLogConsistency(java.util.Set<Long> supplyIds, String relatedNo) {
        for (Long supplyId : supplyIds) {
            stockLogService.validateStockLogIntegrity(supplyId);
        }
        logger.info("库存流水一致性校验通过 - relatedNo: {}, supplyCount: {}", relatedNo, supplyIds.size());
    }
}
