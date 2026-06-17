package com.office.supplies.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.office.supplies.common.ApprovalStatus;
import com.office.supplies.common.BizType;
import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.common.UserContext;
import com.office.supplies.entity.Requisition;
import com.office.supplies.entity.RequisitionItem;
import com.office.supplies.entity.User;
import com.office.supplies.mapper.RequisitionItemMapper;
import com.office.supplies.mapper.RequisitionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RequisitionService extends ServiceImpl<RequisitionMapper, Requisition> {

    @Resource
    private RequisitionMapper requisitionMapper;

    @Resource
    private RequisitionItemMapper requisitionItemMapper;

    @Resource
    private SupplyService supplyService;

    @Resource
    private ApprovalEngineService approvalEngineService;

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    private String generateNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = SEQ.getAndIncrement();
        if (seq > 9999) {
            SEQ.set(1);
            seq = 1;
        }
        return "RQ" + date + String.format("%04d", seq);
    }

    public PageResult<Requisition> getRequisitionPage(PageQuery query, String status, Long departmentId) {
        User user = UserContext.getCurrentUser();
        Long userId = null;
        if ("EMPLOYEE".equals(user.getRoleCode())) {
            userId = user.getId();
        }
        List<Requisition> list = requisitionMapper.getRequisitionList(query.getKeyword(), userId, status, departmentId);
        long total = list.size();
        long start = (query.getCurrent() - 1) * query.getSize();
        long end = Math.min(start + query.getSize(), total);
        List<Requisition> records = list.subList((int) start, (int) end);
        PageResult<Requisition> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        result.setCurrent(query.getCurrent());
        result.setSize(query.getSize());
        return result;
    }

    public Requisition getRequisitionDetail(Long id) {
        Requisition r = requisitionMapper.getRequisitionDetail(id);
        if (r != null) {
            r.setItems(requisitionItemMapper.getItemsByRequisitionId(id));
        }
        return r;
    }

    @Transactional(rollbackFor = Exception.class)
    public Requisition createRequisition(Requisition requisition) {
        User user = UserContext.getCurrentUser();
        requisition.setUserId(user.getId());
        requisition.setDepartmentId(user.getDepartmentId());
        requisition.setRequisitionNo(generateNo());
        requisition.setStatus(ApprovalStatus.PENDING_LEVEL + "1");
        this.save(requisition);
        if (requisition.getItems() != null) {
            for (RequisitionItem item : requisition.getItems()) {
                item.setRequisitionId(requisition.getId());
                if (item.getUnitPrice() == null) {
                    item.setUnitPrice(BigDecimal.ZERO);
                }
                if (item.getQuantity() != null && item.getUnitPrice() != null) {
                    item.setTotalPrice(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                }
                requisitionItemMapper.insert(item);
            }
        }
        BigDecimal totalAmount = calculateTotalAmount(requisition.getId());
        Long categoryId = getFirstCategoryId(requisition.getId());
        approvalEngineService.initApproval(
                BizType.REQUISITION,
                requisition.getId(),
                requisition.getRequisitionNo(),
                user.getId(),
                user.getDepartmentId(),
                categoryId,
                totalAmount
        );
        return requisition;
    }

    private BigDecimal calculateTotalAmount(Long requisitionId) {
        List<RequisitionItem> items = requisitionItemMapper.getItemsByRequisitionId(requisitionId);
        BigDecimal total = BigDecimal.ZERO;
        for (RequisitionItem item : items) {
            if (item.getTotalPrice() != null) {
                total = total.add(item.getTotalPrice());
            }
        }
        return total;
    }

    private Long getFirstCategoryId(Long requisitionId) {
        List<RequisitionItem> items = requisitionItemMapper.getItemsByRequisitionId(requisitionId);
        if (items != null && !items.isEmpty() && items.get(0).getSupplyId() != null) {
            com.office.supplies.entity.Supply supply = supplyService.getById(items.get(0).getSupplyId());
            if (supply != null) {
                return supply.getCategoryId();
            }
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void approveRequisition(Long id, String approveStatus, String approveRemark) {
        Requisition r = this.getById(id);
        if (r == null) {
            throw new RuntimeException("申领单不存在");
        }
        if (!ApprovalStatus.isPendingStatus(r.getStatus())) {
            throw new RuntimeException("申领单状态不允许审批");
        }
        approvalEngineService.approve(BizType.REQUISITION, id, approveStatus, approveRemark, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelRequisition(Long id) {
        Requisition r = this.getById(id);
        if (r == null) {
            throw new RuntimeException("申领单不存在");
        }
        if (!ApprovalStatus.isPendingStatus(r.getStatus())) {
            throw new RuntimeException("只有待审批的申领单可以取消");
        }
        r.setStatus(ApprovalStatus.CANCELLED);
        this.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdrawApproval(Long id, String remark) {
        approvalEngineService.withdraw(BizType.REQUISITION, id, remark);
    }

    @Transactional(rollbackFor = Exception.class)
    public void transferApproval(Long id, String remark, Long transferToUserId) {
        approvalEngineService.approve(BizType.REQUISITION, id, "TRANSFER", remark, transferToUserId);
    }
}
