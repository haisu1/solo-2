package com.office.supplies.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.common.ApprovalStatus;
import com.office.supplies.common.BizType;
import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.common.PageResultConverter;
import com.office.supplies.common.UserContext;
import com.office.supplies.entity.Purchase;
import com.office.supplies.entity.PurchaseItem;
import com.office.supplies.entity.User;
import com.office.supplies.mapper.PurchaseItemMapper;
import com.office.supplies.mapper.PurchaseMapper;
import com.office.supplies.query.PurchaseQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class PurchaseService extends ServiceImpl<PurchaseMapper, Purchase> {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    @Resource
    private PurchaseMapper purchaseMapper;

    @Resource
    private PurchaseItemMapper purchaseItemMapper;

    @Resource
    private SupplyService supplyService;

    @Resource
    private ApprovalEngineService approvalEngineService;

    @Resource
    private SequenceGeneratorService sequenceGeneratorService;

    private static final String SEQ_NAME = "PURCHASE";

    private String generateNo() {
        return sequenceGeneratorService.generateNo("PU", SEQ_NAME);
    }

    @Deprecated
    public PageResult<Purchase> getPurchasePage(PageQuery query, String status) {
        PurchaseQuery purchaseQuery = PurchaseQuery.builder()
                .current(query.getCurrent())
                .size(query.getSize())
                .keyword(query.getKeyword())
                .status(status)
                .build();
        return getPurchasePage(purchaseQuery);
    }

    public PageResult<Purchase> getPurchasePage(PurchaseQuery query) {
        long startTime = System.currentTimeMillis();
        Page<Purchase> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<Purchase> resultPage = purchaseMapper.selectPurchasePage(page, query);
        long costTime = System.currentTimeMillis() - startTime;
        if (costTime > 1000) {
            logger.warn("Slow query detected - getPurchasePage cost: {}ms, params: {}", costTime, query);
        }
        return PageResultConverter.convert(resultPage, query.getCurrent(), query.getSize());
    }

    public Purchase getPurchaseDetail(Long id) {
        Purchase p = purchaseMapper.getPurchaseDetail(id);
        if (p != null) {
            p.setItems(purchaseItemMapper.getItemsByPurchaseId(id));
        }
        return p;
    }

    @Transactional(rollbackFor = Exception.class)
    public Purchase createPurchase(Purchase purchase) {
        User user = UserContext.getCurrentUser();
        purchase.setPurchaseNo(generateNo());
        purchase.setCreatedBy(user.getId());
        purchase.setStatus(ApprovalStatus.PENDING_LEVEL + "1");
        BigDecimal total = BigDecimal.ZERO;
        if (purchase.getItems() != null) {
            for (PurchaseItem item : purchase.getItems()) {
                if (item.getUnitPrice() == null) item.setUnitPrice(BigDecimal.ZERO);
                item.setTotalPrice(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                total = total.add(item.getTotalPrice());
            }
        }
        purchase.setTotalAmount(total);
        this.save(purchase);
        if (purchase.getItems() != null) {
            for (PurchaseItem item : purchase.getItems()) {
                item.setPurchaseId(purchase.getId());
                purchaseItemMapper.insert(item);
            }
        }
        Long categoryId = getFirstCategoryId(purchase.getId());
        approvalEngineService.initApproval(
                BizType.PURCHASE,
                purchase.getId(),
                purchase.getPurchaseNo(),
                user.getId(),
                user.getDepartmentId(),
                categoryId,
                total
        );
        return purchase;
    }

    private Long getFirstCategoryId(Long purchaseId) {
        List<PurchaseItem> items = purchaseItemMapper.getItemsByPurchaseId(purchaseId);
        if (items != null && !items.isEmpty() && items.get(0).getSupplyId() != null) {
            com.office.supplies.entity.Supply supply = supplyService.getById(items.get(0).getSupplyId());
            if (supply != null) {
                return supply.getCategoryId();
            }
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void approvePurchase(Long id, String approveStatus, String approveRemark) {
        Purchase p = this.getById(id);
        if (p == null) {
            throw new RuntimeException("采购单不存在");
        }
        if (!ApprovalStatus.isPendingStatus(p.getStatus())) {
            throw new RuntimeException("采购单状态不允许审批");
        }
        approvalEngineService.approve(BizType.PURCHASE, id, approveStatus, approveRemark, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void stockIn(Long id) {
        Purchase p = this.getById(id);
        if (p == null) {
            throw new RuntimeException("采购单不存在");
        }
        if (!ApprovalStatus.APPROVED.equals(p.getStatus())) {
            throw new RuntimeException("只有已审批的采购单可以入库");
        }
        List<PurchaseItem> items = purchaseItemMapper.getItemsByPurchaseId(id);
        Map<Long, Integer> stockInItems = new java.util.HashMap<>();
        for (PurchaseItem item : items) {
            stockInItems.merge(item.getSupplyId(), item.getQuantity(), Integer::sum);
        }
        for (Map.Entry<Long, Integer> entry : stockInItems.entrySet()) {
            supplyService.addStock(entry.getKey(), entry.getValue(), p.getPurchaseNo(), "采购入库");
        }
        p.setStatus(ApprovalStatus.STOCKED);
        this.updateById(p);
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdrawApproval(Long id, String remark) {
        approvalEngineService.withdraw(BizType.PURCHASE, id, remark);
    }

    @Transactional(rollbackFor = Exception.class)
    public void transferApproval(Long id, String remark, Long transferToUserId) {
        approvalEngineService.approve(BizType.PURCHASE, id, "TRANSFER", remark, transferToUserId);
    }
}
