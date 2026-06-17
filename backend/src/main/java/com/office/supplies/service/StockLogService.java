package com.office.supplies.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.common.UserContext;
import com.office.supplies.entity.StockLog;
import com.office.supplies.entity.Supply;
import com.office.supplies.mapper.StockLogMapper;
import com.office.supplies.mapper.SupplyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StockLogService extends ServiceImpl<StockLogMapper, StockLog> {

    private static final Logger logger = LoggerFactory.getLogger(StockLogService.class);

    @Resource
    private StockLogMapper stockLogMapper;

    @Resource
    private SupplyMapper supplyMapper;

    public List<StockLog> getStockLogList(Long supplyId, String operationType) {
        return stockLogMapper.getStockLogList(supplyId, operationType);
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public void recordStockLog(Long supplyId, String operationType, Integer quantity,
                               Integer beforeStock, Integer afterStock, String relatedNo, String remark) {
        if (afterStock < 0) {
            logger.error("库存为负数，拒绝记录流水 - supplyId: {}, operationType: {}, afterStock: {}",
                    supplyId, operationType, afterStock);
            throw new RuntimeException("库存不能为负数");
        }
        
        if (beforeStock + (operationType.contains("IN") || operationType.equals("ADJUST_IN") ? quantity : -quantity) != afterStock) {
            Supply supply = supplyMapper.selectById(supplyId);
            String supplyName = supply != null ? supply.getSupplyName() : "未知";
            logger.error("库存流水记录校验失败 - supplyId: {}, supplyName: {}, operationType: {}, " +
                            "before: {}, quantity: {}, after: {}, expected: {}",
                    supplyId, supplyName, operationType, beforeStock, quantity, afterStock,
                    beforeStock + (operationType.contains("IN") || operationType.equals("ADJUST_IN") ? quantity : -quantity));
            throw new RuntimeException("库存流水记录校验失败，请联系管理员");
        }
        
        StockLog log = new StockLog();
        log.setSupplyId(supplyId);
        log.setOperationType(operationType);
        log.setQuantity(quantity);
        log.setBeforeStock(beforeStock);
        log.setAfterStock(afterStock);
        log.setRelatedNo(relatedNo);
        log.setOperatorId(UserContext.getCurrentUserId());
        log.setRemark(remark);
        
        int rows = stockLogMapper.insert(log);
        if (rows == 0) {
            logger.error("库存流水记录插入失败 - supplyId: {}, operationType: {}, relatedNo: {}",
                    supplyId, operationType, relatedNo);
            throw new RuntimeException("库存流水记录插入失败");
        }
        
        logger.info("库存流水记录成功 - supplyId: {}, operationType: {}, quantity: {}, before: {}, after: {}, relatedNo: {}",
                supplyId, operationType, quantity, beforeStock, afterStock, relatedNo);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean validateStockLogIntegrity(Long supplyId) {
        List<StockLog> logs = stockLogMapper.getStockLogList(supplyId, null);
        if (logs.isEmpty()) {
            return true;
        }
        
        Supply supply = supplyMapper.selectById(supplyId);
        if (supply == null) {
            throw new RuntimeException("物资不存在");
        }
        
        int expectedStock = supply.getStock();
        int calculatedStock = logs.get(0).getAfterStock();
        
        for (int i = 1; i < logs.size(); i++) {
            StockLog current = logs.get(i);
            StockLog previous = logs.get(i - 1);
            
            if (current.getBeforeStock() != previous.getAfterStock()) {
                logger.error("库存流水不连续 - supplyId: {}, logId: {}, before: {}, previousAfter: {}",
                        supplyId, current.getId(), current.getBeforeStock(), previous.getAfterStock());
                return false;
            }
            calculatedStock = current.getAfterStock();
        }
        
        if (calculatedStock != expectedStock) {
            logger.error("库存流水与实际库存不一致 - supplyId: {}, calculated: {}, actual: {}",
                    supplyId, calculatedStock, expectedStock);
            return false;
        }
        
        logger.info("库存流水完整性校验通过 - supplyId: {}, stock: {}, logCount: {}",
                supplyId, expectedStock, logs.size());
        return true;
    }
}
