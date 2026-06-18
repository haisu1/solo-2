package com.office.supplies.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.common.PageResultConverter;
import com.office.supplies.common.UserContext;
import com.office.supplies.entity.InventoryCheck;
import com.office.supplies.entity.InventoryCheckItem;
import com.office.supplies.entity.Supply;
import com.office.supplies.entity.User;
import com.office.supplies.mapper.InventoryCheckItemMapper;
import com.office.supplies.mapper.InventoryCheckMapper;
import com.office.supplies.query.InventoryCheckQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryCheckService extends ServiceImpl<InventoryCheckMapper, InventoryCheck> {

    private static final Logger logger = LoggerFactory.getLogger(InventoryCheckService.class);

    @Resource
    private InventoryCheckMapper inventoryCheckMapper;

    @Resource
    private InventoryCheckItemMapper inventoryCheckItemMapper;

    @Resource
    private SupplyService supplyService;

    @Resource
    private SequenceGeneratorService sequenceGeneratorService;

    private static final String SEQ_NAME = "INVENTORY_CHECK";

    private String generateNo() {
        return sequenceGeneratorService.generateNo("IC", SEQ_NAME);
    }

    @Deprecated
    public PageResult<InventoryCheck> getCheckPage(PageQuery query, String status) {
        InventoryCheckQuery checkQuery = InventoryCheckQuery.builder()
                .current(query.getCurrent())
                .size(query.getSize())
                .keyword(query.getKeyword())
                .status(status)
                .build();
        return getCheckPage(checkQuery);
    }

    public PageResult<InventoryCheck> getCheckPage(InventoryCheckQuery query) {
        long startTime = System.currentTimeMillis();
        Page<InventoryCheck> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<InventoryCheck> resultPage = inventoryCheckMapper.selectInventoryCheckPage(page, query);
        long costTime = System.currentTimeMillis() - startTime;
        if (costTime > 1000) {
            logger.warn("Slow query detected - getCheckPage cost: {}ms, params: {}", costTime, query);
        }
        return PageResultConverter.convert(resultPage, query.getCurrent(), query.getSize());
    }

    public InventoryCheck getCheckDetail(Long id) {
        InventoryCheck c = inventoryCheckMapper.getCheckDetail(id);
        if (c != null) {
            c.setItems(inventoryCheckItemMapper.getItemsByCheckId(id));
        }
        return c;
    }

    @Transactional(rollbackFor = Exception.class)
    public InventoryCheck createCheck(InventoryCheck check) {
        User user = UserContext.getCurrentUser();
        check.setCheckNo(generateNo());
        check.setCheckedBy(user.getId());
        check.setStatus("DRAFT");
        this.save(check);
        List<Supply> supplies = supplyService.list();
        for (Supply s : supplies) {
            InventoryCheckItem item = new InventoryCheckItem();
            item.setCheckId(check.getId());
            item.setSupplyId(s.getId());
            item.setSystemStock(s.getStock());
            item.setActualStock(s.getStock());
            item.setDiffQuantity(0);
            inventoryCheckItemMapper.insert(item);
        }
        return check;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCheckItems(Long checkId, List<InventoryCheckItem> items) {
        for (InventoryCheckItem item : items) {
            if (item.getId() != null) {
                if (item.getActualStock() == null) item.setActualStock(0);
                if (item.getSystemStock() == null) item.setSystemStock(0);
                item.setDiffQuantity(item.getActualStock() - item.getSystemStock());
                inventoryCheckItemMapper.updateById(item);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeCheck(Long id) {
        InventoryCheck c = this.getById(id);
        if (c == null) {
            throw new RuntimeException("盘点单不存在");
        }
        if (!"DRAFT".equals(c.getStatus())) {
            throw new RuntimeException("只有草稿状态的盘点单可以完成");
        }
        
        List<InventoryCheckItem> items = inventoryCheckItemMapper.getItemsByCheckId(id);
        java.util.Map<Long, Integer> stockAdjustments = new java.util.HashMap<>();
        for (InventoryCheckItem item : items) {
            if (item.getActualStock() == null) {
                item.setActualStock(0);
            }
            if (item.getSystemStock() == null) {
                item.setSystemStock(0);
            }
            int diff = item.getActualStock() - item.getSystemStock();
            item.setDiffQuantity(diff);
            if (diff != 0) {
                stockAdjustments.put(item.getSupplyId(), diff);
            }
            inventoryCheckItemMapper.updateById(item);
        }
        
        if (!stockAdjustments.isEmpty()) {
            for (java.util.Map.Entry<Long, Integer> entry : stockAdjustments.entrySet()) {
                Long supplyId = entry.getKey();
                Integer diffQuantity = entry.getValue();
                String remark = diffQuantity > 0 ? "盘点盈余" : "盘点亏损";
                supplyService.adjustStock(supplyId, diffQuantity, c.getCheckNo(), remark);
            }
        }
        
        c.setStatus("COMPLETED");
        this.updateById(c);
    }
}
