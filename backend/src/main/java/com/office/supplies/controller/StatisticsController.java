package com.office.supplies.controller;

import com.office.supplies.common.Result;
import com.office.supplies.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        return Result.success(statisticsService.getDashboardData());
    }

    @GetMapping("/requisitions")
    public Result<Map<String, Object>> getRequisitionStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statisticsService.getRequisitionStatistics(startDate, endDate));
    }
}
