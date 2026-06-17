package com.office.supplies.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
        requisition.setStatus("PENDING");
        this.save(requisition);
        if (requisition.getItems() != null) {
            for (RequisitionItem item : requisition.getItems()) {
                item.setRequisitionId(requisition.getId());
                requisitionItemMapper.insert(item);
            }
        }
        return requisition;
    }

    @Transactional(rollbackFor = Exception.class)
    public void approveRequisition(Long id, String approveStatus, String approveRemark) {
        Requisition r = this.getById(id);
        if (r == null) {
            throw new RuntimeException("申领单不存在");
        }
        if (!"PENDING".equals(r.getStatus())) {
            throw new RuntimeException("申领单状态不允许审批");
        }
        User user = UserContext.getCurrentUser();
        
        if ("APPROVED".equals(approveStatus)) {
            List<RequisitionItem> items = requisitionItemMapper.getItemsByRequisitionId(id);
            java.util.Map<Long, Integer> stockRequirements = new java.util.HashMap<>();
            for (RequisitionItem item : items) {
                stockRequirements.merge(item.getSupplyId(), item.getQuantity(), Integer::sum);
            }
            supplyService.checkStockAvailability(stockRequirements);
            supplyService.batchReduceStock(stockRequirements, r.getRequisitionNo(), "领用发放");
        }
        
        r.setApprovedBy(user.getId());
        r.setApproveTime(LocalDateTime.now());
        r.setApproveRemark(approveRemark);
        r.setStatus(approveStatus);
        this.updateById(r);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelRequisition(Long id) {
        Requisition r = this.getById(id);
        if (r == null) {
            throw new RuntimeException("申领单不存在");
        }
        if (!"PENDING".equals(r.getStatus())) {
            throw new RuntimeException("只有待审批的申领单可以取消");
        }
        r.setStatus("CANCELLED");
        this.updateById(r);
    }
}
