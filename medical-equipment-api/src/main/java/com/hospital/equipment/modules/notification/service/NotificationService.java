package com.hospital.equipment.modules.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.equipment.common.result.PageResult;
import com.hospital.equipment.modules.notification.entity.Notification;
import com.hospital.equipment.modules.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 通知服务
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    /**
     * 我的通知列表
     */
    public PageResult<Notification> myNotifications(Long userId, long pageNum, long pageSize) {
        QueryWrapper<Notification> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");

        Page<Notification> page = notificationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    /**
     * 标记已读
     */
    public void markRead(Long id, Long userId) {
        Notification n = notificationMapper.selectById(id);
        if (n == null || !n.getUserId().equals(userId)) {
            throw new com.hospital.equipment.common.exception.BusinessException(404, "通知不存在");
        }
        n.setIsRead(1);
        notificationMapper.updateById(n);
    }

    /**
     * 获取未读数
     */
    public long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(
                new QueryWrapper<Notification>().eq("user_id", userId).eq("is_read", 0));
    }
}
