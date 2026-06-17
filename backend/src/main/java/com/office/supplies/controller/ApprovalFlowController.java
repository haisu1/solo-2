package com.office.supplies.controller;

import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.common.Result;
import com.office.supplies.dto.ApprovalFlowDTO;
import com.office.supplies.entity.ApprovalFlow;
import com.office.supplies.service.ApprovalFlowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/approval/flow")
public class ApprovalFlowController {

    @Resource
    private ApprovalFlowService approvalFlowService;

    @GetMapping("/page")
    public Result<PageResult<ApprovalFlow>> getFlowPage(@RequestParam(defaultValue = "1") Long current,
                                                        @RequestParam(defaultValue = "10") Long size,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) String bizType) {
        PageQuery query = new PageQuery();
        query.setCurrent(current);
        query.setSize(size);
        query.setKeyword(keyword);
        return Result.success(approvalFlowService.getFlowPage(query, bizType));
    }

    @GetMapping("/{id}")
    public Result<ApprovalFlow> getFlowDetail(@PathVariable Long id) {
        return Result.success(approvalFlowService.getFlowDetail(id));
    }

    @PostMapping
    public Result<ApprovalFlow> createFlow(@RequestBody ApprovalFlowDTO dto) {
        return Result.success(approvalFlowService.createFlow(dto));
    }

    @PutMapping("/{id}")
    public Result<ApprovalFlow> updateFlow(@PathVariable Long id,
                                           @RequestBody ApprovalFlowDTO dto) {
        return Result.success(approvalFlowService.updateFlow(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFlow(@PathVariable Long id) {
        approvalFlowService.deleteFlow(id);
        return Result.success();
    }
}
