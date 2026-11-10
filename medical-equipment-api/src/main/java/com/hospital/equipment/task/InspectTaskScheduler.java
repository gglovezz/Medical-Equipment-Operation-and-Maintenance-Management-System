package com.hospital.equipment.task;

import com.hospital.equipment.common.constant.DeviceStatus;
import com.hospital.equipment.modules.device.entity.MedicalDevice;
import com.hospital.equipment.modules.device.mapper.DeviceMapper;
import com.hospital.equipment.modules.inspect.service.InspectService;
import com.hospital.equipment.modules.notification.entity.Notification;
import com.hospital.equipment.modules.notification.mapper.NotificationMapper;
import com.hospital.equipment.modules.user.entity.SysUser;
import com.hospital.equipment.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 定时任务调度器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InspectTaskScheduler {

    private final InspectService inspectService;
    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;

    /**
     * 每天凌晨 00:05 自动生成巡检任务
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void generateDailyInspectTasks() {
        log.info("===== 开始执行巡检任务生成定时任务 =====");
        try {
            int count = inspectService.generateDailyTasks();
            log.info("===== 巡检任务生成完成，共生成 {} 条 =====", count);
        } catch (Exception e) {
            log.error("巡检任务生成失败", e);
        }
    }

    /**
     * 每天 08:00 检查计量检定到期
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkVerifyExpire() {
        log.info("===== 开始检查计量检定到期设备 =====");
        try {
            LocalDate today = LocalDate.now();
            LocalDate threshold = today.plusDays(30);

            List<MedicalDevice> devices = deviceMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MedicalDevice>()
                            .isNotNull("verify_expire_date")
                            .between("verify_expire_date", today, threshold)
                            .ne("status", DeviceStatus.DISABLED.getCode())
            );

            if (devices.isEmpty()) {
                log.info("没有即将到期的计量检定设备");
                return;
            }

            // 给所有 admin 和 engineer 发通知
            List<SysUser> receivers = userMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysUser>()
                            .in("role", "admin", "engineer")
                            .eq("status", 1)
            );

            String deviceNames = devices.stream()
                    .map(d -> d.getDeviceName() + "(" + d.getSnCode() + ")")
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            for (SysUser user : receivers) {
                Notification n = new Notification();
                n.setUserId(user.getId());
                n.setTitle("计量检定到期提醒");
                n.setContent("以下 " + devices.size() + " 台设备即将到期: " + deviceNames);
                n.setType("VERIFY_EXPIRE");
                n.setIsRead(0);
                notificationMapper.insert(n);
            }

            log.info("已发送 {} 条计量检定到期通知", receivers.size());
        } catch (Exception e) {
            log.error("检查计量检定到期失败", e);
        }
    }
}
