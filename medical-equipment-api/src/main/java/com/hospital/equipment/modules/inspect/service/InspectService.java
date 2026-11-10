package com.hospital.equipment.modules.inspect.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.equipment.common.constant.OrderStatus;
import com.hospital.equipment.common.result.PageResult;
import com.hospital.equipment.modules.device.entity.MedicalDevice;
import com.hospital.equipment.modules.device.mapper.DeviceMapper;
import com.hospital.equipment.modules.inspect.entity.InspectTask;
import com.hospital.equipment.modules.inspect.mapper.InspectTaskMapper;
import com.hospital.equipment.modules.notification.entity.Notification;
import com.hospital.equipment.modules.notification.mapper.NotificationMapper;
import com.hospital.equipment.modules.repairorder.entity.RepairOrder;
import com.hospital.equipment.modules.repairorder.mapper.RepairOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 巡检任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InspectService {

    private final InspectTaskMapper taskMapper;
    private final DeviceMapper deviceMapper;
    private final RepairOrderMapper orderMapper;
    private final NotificationMapper notificationMapper;

    /**
     * 我的巡检任务列表
     */
    public PageResult<InspectTask> myTasks(long pageNum, long pageSize, Long inspectorId, Integer status) {
        QueryWrapper<InspectTask> wrapper = new QueryWrapper<>();
        wrapper.eq("inspector_id", inspectorId);
        if (status != null && status != 0) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("task_date");

        Page<InspectTask> page = taskMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    /**
     * 提交巡检结果
     */
    @Transactional
    public void submit(Long taskId, Long inspectorId, Integer result, String remark, List<String> imageUrls) {
        InspectTask task = taskMapper.selectById(taskId);
        if (task == null) throw new com.hospital.equipment.common.exception.BusinessException(404, "巡检任务不存在");
        if (task.getStatus() != 1) throw new com.hospital.equipment.common.exception.BusinessException(400, "任务已处理");

        task.setStatus(2); // 已完成
        task.setResult(result);
        task.setRemark(remark);
        if (imageUrls != null && !imageUrls.isEmpty()) {
            task.setImages(String.join(",", imageUrls));
        }
        taskMapper.updateById(task);

        // 如果异常，自动转报修
        if (result == 2) {
            task.setStatus(3);
            taskMapper.updateById(task);

            RepairOrder order = new RepairOrder();
            order.setOrderNo(generateOrderNo());
            order.setDeviceId(task.getDeviceId());
            order.setReporterId(inspectorId);
            order.setDescription("巡检异常自动转报修: " + remark);
            order.setStatus(OrderStatus.PENDING.getCode());
            orderMapper.insert(order);

            // 设备状态改为维修中
            MedicalDevice device = deviceMapper.selectById(task.getDeviceId());
            if (device != null) {
                device.setStatus(2);
                deviceMapper.updateById(device);
            }
        }

        // 更新设备巡检日期
        MedicalDevice device = deviceMapper.selectById(task.getDeviceId());
        if (device != null) {
            device.setLastInspectDate(LocalDate.now());
            // 下次巡检日期设为30天后
            device.setNextInspectDate(LocalDate.now().plusDays(30));
            deviceMapper.updateById(device);
        }
    }

    /**
     * 生成巡检任务（定时任务调用）
     */
    @Transactional
    public int generateDailyTasks() {
        LocalDate today = LocalDate.now();
        List<MedicalDevice> devices = deviceMapper.selectList(
                new QueryWrapper<MedicalDevice>()
                        .le("next_inspect_date", today)
                        .eq("status", 1) // 正常使用
        );

        int count = 0;
        for (MedicalDevice device : devices) {
            // 检查今天是否已生成任务
            Long exists = taskMapper.selectCount(
                    new QueryWrapper<InspectTask>()
                            .eq("device_id", device.getId())
                            .eq("task_date", today)
                            .in("status", 1, 2)
            );
            if (exists > 0) continue;

            InspectTask task = new InspectTask();
            task.setDeviceId(device.getId());
            task.setTaskDate(today);
            task.setStatus(1); // 待巡检
            // 简化：暂不分配具体工程师
            taskMapper.insert(task);
            count++;
        }
        log.info("自动生成巡检任务 {} 条", count);
        return count;
    }

    private synchronized String generateOrderNo() {
        String dateStr = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "BX" + dateStr;
        Long maxSeq = orderMapper.selectCount(
                new QueryWrapper<RepairOrder>().likeRight("order_no", prefix));
        int seq = (maxSeq != null ? maxSeq.intValue() : 0) + 1;
        return prefix + String.format("%04d", seq);
    }
}
