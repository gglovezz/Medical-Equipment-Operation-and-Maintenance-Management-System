package com.hospital.equipment.modules.notification.controller;

import com.hospital.equipment.common.result.PageResult;
import com.hospital.equipment.common.result.Result;
import com.hospital.equipment.common.utils.SecurityUtils;
import com.hospital.equipment.modules.notification.entity.Notification;
import com.hospital.equipment.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    public Result<PageResult<Notification>> myNotifications(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(notificationService.myNotifications(userId, pageNum, pageSize));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationService.markRead(id, userId);
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(notificationService.getUnreadCount(userId));
    }
}
