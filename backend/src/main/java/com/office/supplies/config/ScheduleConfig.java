package com.office.supplies.config;

import com.office.supplies.service.InventoryWarningService;
import com.office.supplies.service.WarningNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Configuration
@EnableScheduling
public class ScheduleConfig {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleConfig.class);

    @Resource
    private com.office.supplies.service.ApprovalEngineService approvalEngineService;

    @Resource
    private InventoryWarningService inventoryWarningService;

    @Resource
    private WarningNotificationService warningNotificationService;

    @Scheduled(cron = "0 0 8,12,18 * * ?")
    public void processApprovalTimeoutReminder() {
        approvalEngineService.processTimeoutReminders();
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void calculateDailyInventoryWarning() {
        logger.info("开始执行每日库存预警计算任务 - {}", LocalDateTime.now());
        try {
            inventoryWarningService.calculateAllWarnings();
            warningNotificationService.generateWarningNotifications();
            logger.info("每日库存预警计算任务执行完成");
        } catch (Exception e) {
            logger.error("每日库存预警计算任务执行失败", e);
        }
    }
}
