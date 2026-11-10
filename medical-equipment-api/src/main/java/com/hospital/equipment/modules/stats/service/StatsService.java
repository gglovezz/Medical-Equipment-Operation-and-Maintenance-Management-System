package com.hospital.equipment.modules.stats.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.equipment.modules.device.entity.MedicalDevice;
import com.hospital.equipment.modules.device.mapper.DeviceMapper;
import com.hospital.equipment.modules.repairorder.entity.RepairOrder;
import com.hospital.equipment.modules.repairorder.mapper.RepairOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

/**
 * 统计报表服务
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final DeviceMapper deviceMapper;
    private final RepairOrderMapper orderMapper;

    /**
     * 获取概览统计数据
     */
    public Map<String, Object> overview(String month) {
        YearMonth yearMonth = (month != null && !month.isEmpty()) 
                ? YearMonth.parse(month) 
                : YearMonth.now();
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        Map<String, Object> result = new HashMap<>();

        // 设备总数
        Long deviceTotal = deviceMapper.selectCount(null);
        result.put("deviceTotal", deviceTotal);

        // 设备完好率
        Long normalCount = deviceMapper.selectCount(
                new QueryWrapper<MedicalDevice>().eq("status", 1));
        double normalRate = deviceTotal > 0 ? (double) normalCount / deviceTotal : 0;
        result.put("deviceNormalRate", Math.round(normalRate * 100.0) / 100.0);

        // 本月报修数
        Long repairThisMonth = orderMapper.selectCount(
                new QueryWrapper<RepairOrder>().between("create_time", start, end));
        result.put("repairThisMonth", repairThisMonth);

        // 已完成工单数
        Long repairFinished = orderMapper.selectCount(
                new QueryWrapper<RepairOrder>()
                        .eq("status", 4)
                        .between("create_time", start, end));
        result.put("repairFinished", repairFinished);

        // 待处理工单
        Long repairPending = orderMapper.selectCount(
                new QueryWrapper<RepairOrder>()
                        .in("status", 1, 2, 3)
                        .between("create_time", start, end));
        result.put("repairPending", repairPending);

        // 工单完成率
        double finishRate = repairThisMonth > 0 ? (double) repairFinished / repairThisMonth : 0;
        result.put("repairFinishRate", Math.round(finishRate * 100.0) / 100.0);

        return result;
    }
}
