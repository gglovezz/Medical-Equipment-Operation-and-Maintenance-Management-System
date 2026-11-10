package com.hospital.equipment.modules.device.controller;

import com.hospital.equipment.common.result.PageResult;
import com.hospital.equipment.common.result.Result;
import com.hospital.equipment.modules.device.entity.MedicalDevice;
import com.hospital.equipment.modules.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备管理控制器
 */
@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /**
     * 设备分页列表
     */
    @GetMapping("/page")
    public Result<PageResult<MedicalDevice>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dept,
            @RequestParam(required = false) Integer status) {
        return Result.success(deviceService.page(pageNum, pageSize, keyword, dept, status));
    }

    /**
     * 设备详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(deviceService.getDetail(id));
    }

    /**
     * 新增设备
     */
    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Result<MedicalDevice> create(@RequestBody MedicalDevice device) {
        return Result.success(deviceService.create(device));
    }

    /**
     * 编辑设备
     */
    @PutMapping
    @PreAuthorize("hasRole('admin')")
    public Result<MedicalDevice> update(@RequestBody MedicalDevice device) {
        return Result.success(deviceService.update(device));
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return Result.success();
    }

    /**
     * 生成设备二维码
     */
    @GetMapping(value = "/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrcode(@PathVariable Long id) {
        return deviceService.generateQrCode(id);
    }

    /**
     * 所有设备列表（不分页）
     */
    @GetMapping("/all")
    public Result<List<MedicalDevice>> listAll() {
        return Result.success(deviceService.listAll());
    }
}
