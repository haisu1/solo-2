package com.office.supplies.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;

@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Resource
    private com.office.supplies.service.ApprovalEngineService approvalEngineService;

    @Scheduled(cron = "0 0 8,12,18 * * ?")
    public void processApprovalTimeoutReminder() {
        approvalEngineService.processTimeoutReminders();
    }
}
