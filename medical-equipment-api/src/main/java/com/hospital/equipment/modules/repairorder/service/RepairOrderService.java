package com.hospital.equipment.modules.repairorder.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.equipment.common.constant.DeviceStatus;
import com.hospital.equipment.common.constant.OrderStatus;
import com.hospital.equipment.common.exception.BusinessException;
import com.hospital.equipment.common.result.PageResult;
import com.hospital.equipment.modules.device.entity.MedicalDevice;
import com.hospital.equipment.modules.device.mapper.DeviceMapper;
import com.hospital.equipment.modules.notification.entity.Notification;
import com.hospital.equipment.modules.notification.mapper.NotificationMapper;
import com.hospital.equipment.modules.repairorder.entity.RepairOrder;
import com.hospital.equipment.modules.repairorder.entity.RepairOrderLog;
import com.hospital.equipment.modules.repairorder.mapper.RepairOrderLogMapper;
import com.hospital.equipment.modules.repairorder.mapper.RepairOrderMapper;
import com.hospital.equipment.modules.user.entity.SysUser;
import com.hospital.equipment.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 报修工单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepairOrderService {

    private final RepairOrderMapper orderMapper;
    private final RepairOrderLogMapper logMapper;
    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;

    /**
     * 生成工单编号: BX + yyyyMMdd + 4位序号
     */
    public synchronized String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "BX" + dateStr;

        Long maxSeq = orderMapper.selectCount(
                new QueryWrapper<RepairOrder>().likeRight("order_no", prefix));
        int seq = (maxSeq != null ? maxSeq.intValue() : 0) + 1;
        return prefix + String.format("%04d", seq);
    }

    /**
     * 提交报修
     */
    @Transactional
    public RepairOrder submit(Long deviceId, Long reporterId, String description, List<String> imageUrls) {
        MedicalDevice device = deviceMapper.selectById(deviceId);
        if (device == null) throw new BusinessException(404, "设备不存在");

        RepairOrder order = new RepairOrder();
        order.setOrderNo(generateOrderNo());
        order.setDeviceId(deviceId);
        order.setReporterId(reporterId);
        order.setDescription(description);
        order.setStatus(OrderStatus.PENDING.getCode());
        if (imageUrls != null && !imageUrls.isEmpty()) {
            order.setImages(String.join(",", imageUrls));
        }
        orderMapper.insert(order);

        // 记录操作日志
        saveLog(order.getId(), reporterId, "SUBMIT", "提交报修单");

        // 设备状态改为维修中
        device.setStatus(DeviceStatus.REPAIRING.getCode());
        deviceMapper.updateById(device);

        return order;
    }

    /**
     * 分页查询工单（engineer/admin 用）
     */
    public PageResult<RepairOrder> page(long pageNum, long pageSize, Integer status, String keyword) {
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        if (status != null && status != 0) {
            wrapper.eq("status", status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("order_no", keyword)
                    .or().like("description", keyword));
        }
        wrapper.orderByDesc("create_time");

        Page<RepairOrder> page = orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    /**
     * 我的工单（nurse 用）
     */
    public PageResult<RepairOrder> myPage(long pageNum, long pageSize, Long reporterId, Integer status) {
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("reporter_id", reporterId);
        if (status != null && status != 0) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");

        Page<RepairOrder> page = orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    /**
     * 我的受理工单（engineer 用）
     */
    public PageResult<RepairOrder> assignedPage(long pageNum, long pageSize, Long assigneeId, Integer status) {
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("assignee_id", assigneeId);
        if (status != null && status != 0) {
            wrapper.eq("status", status);
        } else {
            wrapper.in("status", OrderStatus.PENDING.getCode(), OrderStatus.REPAIRING.getCode());
        }
        wrapper.orderByDesc("create_time");

        Page<RepairOrder> page = orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    /**
     * 工单详情
     */
    public Map<String, Object> getDetail(Long id) {
        RepairOrder order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(404, "工单不存在");

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);

        // 设备信息
        result.put("device", deviceMapper.selectById(order.getDeviceId()));

        // 报修人信息
        SysUser reporter = userMapper.selectById(order.getReporterId());
        if (reporter != null) reporter.setPassword(null);
        result.put("reporter", reporter);

        // 处理人信息
        if (order.getAssigneeId() != null) {
            SysUser assignee = userMapper.selectById(order.getAssigneeId());
            if (assignee != null) assignee.setPassword(null);
            result.put("assignee", assignee);
        }

        // 操作日志
        List<RepairOrderLog> logs = logMapper.selectList(
                new QueryWrapper<RepairOrderLog>().eq("order_id", id).orderByAsc("create_time"));
        result.put("logs", logs);

        return result;
    }

    /**
     * 接单
     */
    @Transactional
    public void accept(Long orderId, Long engineerId) {
        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "工单不存在");
        if (!OrderStatus.PENDING.getCode().equals(order.getStatus())) {
            throw new BusinessException(400, "工单状态不是待受理，无法接单");
        }

        order.setStatus(OrderStatus.REPAIRING.getCode());
        order.setAssigneeId(engineerId);
        orderMapper.updateById(order);

        saveLog(orderId, engineerId, "ACCEPT", "接单处理");

        // 通知报修人
        createNotification(order.getReporterId(), "工单已受理",
                "您提交的工单 " + order.getOrderNo() + " 已被工程师接单处理",
                "REPAIR_ACCEPTED", orderId);
    }

    /**
     * 完工
     */
    @Transactional
    public void finish(Long orderId, Long engineerId, String repairRemark, List<String> imageUrls) {
        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "工单不存在");
        if (!OrderStatus.REPAIRING.getCode().equals(order.getStatus())) {
            throw new BusinessException(400, "工单状态不是维修中，无法完工");
        }

        order.setStatus(OrderStatus.PENDING_RATE.getCode());
        order.setRepairRemark(repairRemark);
        if (imageUrls != null && !imageUrls.isEmpty()) {
            String existingImages = order.getImages();
            String newImages = String.join(",", imageUrls);
            order.setImages(existingImages == null ? newImages : existingImages + "," + newImages);
        }
        order.setFinishTime(LocalDateTime.now());
        orderMapper.updateById(order);

        saveLog(orderId, engineerId, "FINISH", "完工处理: " + repairRemark);

        // 设备状态恢复正常
        MedicalDevice device = deviceMapper.selectById(order.getDeviceId());
        if (device != null) {
            device.setStatus(DeviceStatus.NORMAL.getCode());
            deviceMapper.updateById(device);
        }

        // 通知报修人
        createNotification(order.getReporterId(), "工单已完工",
                "您提交的工单 " + order.getOrderNo() + " 已完工，请及时评价",
                "REPAIR_FINISHED", orderId);
    }

    /**
     * 评价
     */
    @Transactional
    public void rate(Long orderId, Long nurseId, Integer rating, String comment) {
        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "工单不存在");
        if (!nurseId.equals(order.getReporterId())) {
            throw new BusinessException(403, "只能评价自己提交的工单");
        }
        if (!OrderStatus.PENDING_RATE.getCode().equals(order.getStatus())) {
            throw new BusinessException(400, "工单状态不是待评价，无法评价");
        }
        if (rating < 1 || rating > 5) {
            throw new BusinessException(400, "评分必须在1-5之间");
        }

        order.setStatus(OrderStatus.FINISHED.getCode());
        order.setRating(rating);
        order.setComment(comment);
        orderMapper.updateById(order);

        saveLog(orderId, nurseId, "RATE", "评价: " + rating + "星, " + comment);
    }

    /**
     * 转派（admin）
     */
    @Transactional
    public void assign(Long orderId, Long adminId, Long engineerId) {
        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "工单不存在");

        SysUser engineer = userMapper.selectById(engineerId);
        if (engineer == null || !"engineer".equals(engineer.getRole())) {
            throw new BusinessException(400, "工程师不存在");
        }

        order.setAssigneeId(engineerId);
        orderMapper.updateById(order);

        saveLog(orderId, adminId, "ASSIGN", "转派给: " + engineer.getRealName());
    }

    /**
     * 关闭（admin）
     */
    @Transactional
    public void close(Long orderId, Long adminId, String closeReason) {
        RepairOrder order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException(404, "工单不存在");
        if (OrderStatus.FINISHED.getCode().equals(order.getStatus()) 
                || OrderStatus.CLOSED.getCode().equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不能关闭");
        }

        order.setStatus(OrderStatus.CLOSED.getCode());
        order.setCloseReason(closeReason);
        orderMapper.updateById(order);

        saveLog(orderId, adminId, "CLOSE", "关闭工单: " + closeReason);
    }

    /**
     * 获取工单的所有图片列表
     */
    public List<String> getImages(RepairOrder order) {
        if (order.getImages() == null || order.getImages().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(order.getImages().split(","));
    }

    private void saveLog(Long orderId, Long operatorId, String action, String detail) {
        RepairOrderLog logEntity = new RepairOrderLog();
        logEntity.setOrderId(orderId);
        logEntity.setOperatorId(operatorId);
        logEntity.setAction(action);
        logEntity.setDetail(detail);
        logMapper.insert(logEntity);
    }

    private void createNotification(Long userId, String title, String content, String type, Long refId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setRefId(refId);
        n.setIsRead(0);
        notificationMapper.insert(n);
    }
}
