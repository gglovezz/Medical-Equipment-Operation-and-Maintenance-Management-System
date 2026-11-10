package com.hospital.equipment.modules.device.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.equipment.common.exception.BusinessException;
import com.hospital.equipment.common.result.PageResult;
import com.hospital.equipment.modules.device.entity.MedicalDevice;
import com.hospital.equipment.modules.device.mapper.DeviceMapper;
import com.hospital.equipment.modules.repairorder.entity.RepairOrder;
import com.hospital.equipment.modules.repairorder.mapper.RepairOrderMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceMapper deviceMapper;
    private final RepairOrderMapper repairOrderMapper;

    /**
     * 分页查询设备
     */
    public PageResult<MedicalDevice> page(long pageNum, long pageSize, String keyword, 
                                           String dept, Integer status) {
        QueryWrapper<MedicalDevice> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("device_name", keyword)
                    .or().like("model", keyword)
                    .or().like("sn_code", keyword));
        }
        if (dept != null && !dept.isEmpty()) {
            wrapper.eq("department", dept);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");

        Page<MedicalDevice> page = deviceMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(page.getTotal(), page.getRecords());
    }

    /**
     * 获取设备详情（包含最近工单）
     */
    public Map<String, Object> getDetail(Long id) {
        MedicalDevice device = deviceMapper.selectById(id);
        if (device == null) throw new BusinessException(404, "设备不存在");

        // 最近5条工单
        List<RepairOrder> recentOrders = repairOrderMapper.selectList(
                new QueryWrapper<RepairOrder>()
                        .eq("device_id", id)
                        .orderByDesc("create_time")
                        .last("limit 5")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("device", device);
        result.put("recentOrders", recentOrders);
        return result;
    }

    /**
     * 新增设备
     */
    public MedicalDevice create(MedicalDevice device) {
        // SN 唯一性校验
        Long count = deviceMapper.selectCount(
                new QueryWrapper<MedicalDevice>().eq("sn_code", device.getSnCode()));
        if (count > 0) throw new BusinessException(2001, "设备SN已存在");
        deviceMapper.insert(device);
        return device;
    }

    /**
     * 编辑设备
     */
    public MedicalDevice update(MedicalDevice device) {
        MedicalDevice existing = deviceMapper.selectById(device.getId());
        if (existing == null) throw new BusinessException(404, "设备不存在");
        deviceMapper.updateById(device);
        return device;
    }

    /**
     * 删除设备
     */
    public void delete(Long id) {
        deviceMapper.deleteById(id);
    }

    /**
     * 更新设备状态
     */
    public void updateStatus(Long id, Integer status) {
        MedicalDevice device = deviceMapper.selectById(id);
        if (device == null) throw new BusinessException(404, "设备不存在");
        device.setStatus(status);
        deviceMapper.updateById(device);
    }

    /**
     * 生成设备二维码
     */
    public byte[] generateQrCode(Long id) {
        MedicalDevice device = deviceMapper.selectById(id);
        if (device == null) throw new BusinessException(404, "设备不存在");

        // 二维码内容: MED-DEVICE-{id}
        String content = "MED-DEVICE-" + id;

        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300, hints);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return baos.toByteArray();
        } catch (WriterException | java.io.IOException e) {
            log.error("生成二维码失败", e);
            throw new BusinessException(500, "生成二维码失败");
        }
    }

    /**
     * 获取所有设备列表
     */
    public List<MedicalDevice> listAll() {
        return deviceMapper.selectList(null);
    }

    /**
     * 根据ID获取设备
     */
    public MedicalDevice getById(Long id) {
        MedicalDevice device = deviceMapper.selectById(id);
        if (device == null) throw new BusinessException(404, "设备不存在");
        return device;
    }
}
