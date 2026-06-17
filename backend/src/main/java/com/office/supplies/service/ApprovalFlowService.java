package com.office.supplies.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.dto.ApprovalFlowDTO;
import com.office.supplies.dto.ApprovalNodeDTO;
import com.office.supplies.entity.ApprovalFlow;
import com.office.supplies.entity.ApprovalNode;
import com.office.supplies.mapper.ApprovalFlowMapper;
import com.office.supplies.mapper.ApprovalNodeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApprovalFlowService extends ServiceImpl<ApprovalFlowMapper, ApprovalFlow> {

    @Resource
    private ApprovalFlowMapper approvalFlowMapper;

    @Resource
    private ApprovalNodeMapper approvalNodeMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PageResult<ApprovalFlow> getFlowPage(PageQuery query, String bizType) {
        List<ApprovalFlow> list = approvalFlowMapper.getFlowList(bizType, query.getKeyword());
        long total = list.size();
        long start = (query.getCurrent() - 1) * query.getSize();
        long end = Math.min(start + query.getSize(), total);
        List<ApprovalFlow> records = list.subList((int) start, (int) end);
        for (ApprovalFlow flow : records) {
            flow.setNodes(approvalNodeMapper.getNodesByFlowId(flow.getId()));
        }
        PageResult<ApprovalFlow> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        result.setCurrent(query.getCurrent());
        result.setSize(query.getSize());
        return result;
    }

    public ApprovalFlow getFlowDetail(Long id) {
        ApprovalFlow flow = this.getById(id);
        if (flow != null) {
            flow.setNodes(approvalNodeMapper.getNodesByFlowId(id));
        }
        return flow;
    }

    public ApprovalFlow getMatchedFlow(String bizType, Long departmentId, Long categoryId, BigDecimal amount) {
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        List<ApprovalFlow> flows = approvalFlowMapper.findMatchingFlows(bizType, departmentId, categoryId, amount);
        if (flows == null || flows.isEmpty()) {
            return buildDefaultFlow(bizType);
        }
        ApprovalFlow flow = flows.get(0);
        flow.setNodes(approvalNodeMapper.getNodesByFlowId(flow.getId()));
        if (flow.getNodes() == null || flow.getNodes().isEmpty()) {
            List<ApprovalNode> defaultNodes = buildDefaultNodes(flow.getId());
            flow.setNodes(defaultNodes);
        }
        return flow;
    }

    private ApprovalFlow buildDefaultFlow(String bizType) {
        ApprovalFlow flow = new ApprovalFlow();
        flow.setId(0L);
        flow.setFlowName("默认审批流程");
        flow.setBizType(bizType);
        flow.setPriority(0);
        flow.setStatus(1);
        flow.setNodes(buildDefaultNodes(0L));
        return flow;
    }

    private List<ApprovalNode> buildDefaultNodes(Long flowId) {
        List<ApprovalNode> nodes = new ArrayList<>();
        ApprovalNode node = new ApprovalNode();
        node.setId(0L);
        node.setFlowId(flowId);
        node.setNodeLevel(1);
        node.setNodeName("部门主管审批");
        node.setApproverType("DEPARTMENT_LEADER");
        node.setTimeoutHours(24);
        node.setCanTransfer(1);
        node.setCanWithdraw(1);
        nodes.add(node);
        return nodes;
    }

    @Transactional(rollbackFor = Exception.class)
    public ApprovalFlow createFlow(ApprovalFlowDTO dto) {
        ApprovalFlow flow = new ApprovalFlow();
        flow.setFlowName(dto.getFlowName());
        flow.setBizType(dto.getBizType());
        flow.setDepartmentId(dto.getDepartmentId());
        flow.setCategoryId(dto.getCategoryId());
        flow.setMinAmount(dto.getMinAmount() != null ? dto.getMinAmount() : BigDecimal.ZERO);
        flow.setMaxAmount(dto.getMaxAmount() != null ? dto.getMaxAmount() : new BigDecimal("99999999.99"));
        flow.setPriority(dto.getPriority() != null ? dto.getPriority() : 0);
        flow.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        if (dto.getNodes() != null) {
            try {
                flow.setNodeConfig(objectMapper.writeValueAsString(dto.getNodes()));
            } catch (JsonProcessingException e) {
                flow.setNodeConfig("[]");
            }
        }
        this.save(flow);
        if (dto.getNodes() != null) {
            saveNodes(flow.getId(), dto.getNodes());
        }
        flow.setNodes(approvalNodeMapper.getNodesByFlowId(flow.getId()));
        return flow;
    }

    @Transactional(rollbackFor = Exception.class)
    public ApprovalFlow updateFlow(Long id, ApprovalFlowDTO dto) {
        ApprovalFlow flow = this.getById(id);
        if (flow == null) {
            throw new RuntimeException("审批流程不存在");
        }
        flow.setFlowName(dto.getFlowName());
        flow.setBizType(dto.getBizType());
        flow.setDepartmentId(dto.getDepartmentId());
        flow.setCategoryId(dto.getCategoryId());
        if (dto.getMinAmount() != null) flow.setMinAmount(dto.getMinAmount());
        if (dto.getMaxAmount() != null) flow.setMaxAmount(dto.getMaxAmount());
        if (dto.getPriority() != null) flow.setPriority(dto.getPriority());
        if (dto.getStatus() != null) flow.setStatus(dto.getStatus());
        if (dto.getNodes() != null) {
            try {
                flow.setNodeConfig(objectMapper.writeValueAsString(dto.getNodes()));
            } catch (JsonProcessingException e) {
                flow.setNodeConfig("[]");
            }
        }
        this.updateById(flow);
        if (dto.getNodes() != null) {
            approvalNodeMapper.deleteByFlowId(id);
            saveNodes(id, dto.getNodes());
        }
        flow.setNodes(approvalNodeMapper.getNodesByFlowId(id));
        return flow;
    }

    private void saveNodes(Long flowId, List<ApprovalNodeDTO> nodeDTOs) {
        int level = 1;
        for (ApprovalNodeDTO nodeDTO : nodeDTOs) {
            ApprovalNode node = new ApprovalNode();
            node.setFlowId(flowId);
            node.setNodeLevel(level++);
            node.setNodeName(nodeDTO.getNodeName());
            node.setApproverType(nodeDTO.getApproverType());
            node.setApproverIds(nodeDTO.getApproverIds());
            node.setRoleCode(nodeDTO.getRoleCode());
            node.setTimeoutHours(nodeDTO.getTimeoutHours() != null ? nodeDTO.getTimeoutHours() : 24);
            node.setCanTransfer(nodeDTO.getCanTransfer() != null ? nodeDTO.getCanTransfer() : 1);
            node.setCanWithdraw(nodeDTO.getCanWithdraw() != null ? nodeDTO.getCanWithdraw() : 1);
            approvalNodeMapper.insert(node);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFlow(Long id) {
        approvalNodeMapper.deleteByFlowId(id);
        this.removeById(id);
    }

    public List<ApprovalNode> parseNodeConfig(String nodeConfig) {
        if (nodeConfig == null || nodeConfig.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(nodeConfig, new TypeReference<List<ApprovalNode>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
