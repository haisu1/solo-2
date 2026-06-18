package com.office.supplies.service;

import com.office.supplies.common.WarningLevel;
import com.office.supplies.dto.SupplyWarningDTO;
import com.office.supplies.dto.WarningMessageDTO;
import com.office.supplies.entity.Role;
import com.office.supplies.entity.User;
import com.office.supplies.entity.WarningMessage;
import com.office.supplies.mapper.RoleMapper;
import com.office.supplies.mapper.UserMapper;
import com.office.supplies.mapper.WarningMessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WarningNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WarningNotificationService.class);

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_ADMIN_STAFF = "ADMIN_STAFF";

    @Resource
    private InventoryWarningService inventoryWarningService;

    @Resource
    private WarningMessageMapper warningMessageMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RoleMapper roleMapper;

    public void generateWarningNotifications() {
        List<SupplyWarningDTO> warnings = inventoryWarningService.getWarningSupplies();

        if (warnings.isEmpty()) {
            logger.info("无预警物资，跳过通知生成");
            return;
        }

        List<User> notifyUsers = getNotifyUsers();

        int generatedCount = 0;
        for (SupplyWarningDTO warning : warnings) {
            for (User user : notifyUsers) {
                if (shouldNotifyUser(user, warning)) {
                    WarningMessage message = createWarningMessage(warning, user);
                    warningMessageMapper.insert(message);
                    sendNotification(user, message);
                    generatedCount++;
                }
            }
        }

        logger.info("预警通知生成完成，共生成 {} 条通知消息", generatedCount);
    }

    private List<User> getNotifyUsers() {
        List<User> allUsers = userMapper.selectList(null);
        Map<Long, Role> roleMap = new HashMap<>();
        for (Role role : roleMapper.selectList(null)) {
            roleMap.put(role.getId(), role);
        }

        for (User user : allUsers) {
            if (user.getRoleId() != null && roleMap.containsKey(user.getRoleId())) {
                user.setRoleCode(roleMap.get(user.getRoleId()).getRoleCode());
                user.setRoleName(roleMap.get(user.getRoleId()).getRoleName());
            }
        }

        return allUsers;
    }

    private boolean shouldNotifyUser(User user, SupplyWarningDTO warning) {
        if (user.getRoleCode() == null) {
            return false;
        }

        WarningLevel level = WarningLevel.valueOf(warning.getWarningLevel());

        if (ROLE_ADMIN.equals(user.getRoleCode())) {
            return true;
        }

        if (ROLE_ADMIN_STAFF.equals(user.getRoleCode())) {
            return level == WarningLevel.WARNING || level == WarningLevel.URGENT;
        }

        return level == WarningLevel.URGENT;
    }

    private WarningMessage createWarningMessage(SupplyWarningDTO warning, User user) {
        WarningMessage message = new WarningMessage();
        message.setSupplyId(warning.getId());
        message.setSupplyName(warning.getSupplyName());
        message.setSupplyCode(warning.getSupplyCode());
        message.setWarningLevel(warning.getWarningLevel());
        message.setWarningContent(buildWarningContent(warning));
        message.setCurrentStock(warning.getStock());
        message.setSafeStockQuantity(warning.getSafeStockQuantity());
        message.setStockDays(warning.getStockDays());
        message.setUserId(user.getId());
        message.setReadFlag(0);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }

    private String buildWarningContent(SupplyWarningDTO warning) {
        StringBuilder sb = new StringBuilder();
        sb.append("物资【").append(warning.getSupplyName()).append("】");
        sb.append("库存预警等级：").append(WarningLevel.getDescByCode(warning.getWarningLevel())).append("；");
        sb.append("当前库存：").append(warning.getStock()).append(warning.getUnit() != null ? warning.getUnit() : "").append("；");
        sb.append("安全库存：").append(warning.getSafeStockQuantity()).append("；");
        if (warning.getStockDays() != null && warning.getStockDays() < 999) {
            sb.append("预计可使用：").append(warning.getStockDays()).append("天；");
        }
        if (warning.getDailyConsumption() != null && warning.getDailyConsumption() > 0) {
            sb.append("日均消耗：").append(String.format("%.2f", warning.getDailyConsumption())).append("；");
        }
        return sb.toString();
    }

    private void sendNotification(User user, WarningMessage message) {
        logger.info("发送预警通知 - 用户：{}({})，物资：{}，等级：{}",
                user.getRealName(), user.getUsername(),
                message.getSupplyName(), message.getWarningLevel());

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            sendEmailNotification(user.getEmail(), message);
        }
    }

    private void sendEmailNotification(String email, WarningMessage message) {
        logger.info("邮件通知模拟发送 - 邮箱：{}，内容：{}", email, message.getWarningContent());
    }

    public List<WarningMessageDTO> getWarningMessages(Long userId, Integer readFlag, String warningLevel) {
        List<WarningMessage> messages = warningMessageMapper.getWarningMessages(userId, readFlag, warningLevel);
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<WarningMessageDTO> getUnreadMessages(Long userId) {
        return getWarningMessages(userId, 0, null);
    }

    public boolean markAsRead(Long id) {
        return warningMessageMapper.markAsRead(id) > 0;
    }

    public boolean markAllAsRead(Long userId) {
        return warningMessageMapper.markAllAsRead(userId) > 0;
    }

    public Map<String, Object> getUnreadStatistics(Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> data = warningMessageMapper.getUnreadCountByLevel(userId);

        int total = 0;
        int urgent = 0, warning = 0, attention = 0;

        for (Map<String, Object> row : data) {
            String level = (String) row.get("warningLevel");
            int count = ((Number) row.get("count")).intValue();
            total += count;

            if (WarningLevel.URGENT.getCode().equals(level)) {
                urgent = count;
            } else if (WarningLevel.WARNING.getCode().equals(level)) {
                warning = count;
            } else if (WarningLevel.ATTENTION.getCode().equals(level)) {
                attention = count;
            }
        }

        result.put("total", total);
        result.put("urgent", urgent);
        result.put("warning", warning);
        result.put("attention", attention);

        return result;
    }

    private WarningMessageDTO convertToDTO(WarningMessage message) {
        WarningMessageDTO dto = new WarningMessageDTO();
        dto.setId(message.getId());
        dto.setSupplyId(message.getSupplyId());
        dto.setSupplyName(message.getSupplyName());
        dto.setSupplyCode(message.getSupplyCode());
        dto.setWarningLevel(message.getWarningLevel());
        dto.setWarningLevelDesc(WarningLevel.getDescByCode(message.getWarningLevel()));
        dto.setWarningContent(message.getWarningContent());
        dto.setCurrentStock(message.getCurrentStock());
        dto.setSafeStockQuantity(message.getSafeStockQuantity());
        dto.setStockDays(message.getStockDays());
        dto.setReadFlag(message.getReadFlag() != null && message.getReadFlag() == 1);
        dto.setReadTime(message.getReadTime());
        dto.setCreateTime(message.getCreateTime());
        return dto;
    }
}
