package com.hospital.equipment.modules.repairorder.controller;

import com.hospital.equipment.common.result.PageResult;
import com.hospital.equipment.common.result.Result;
import com.hospital.equipment.common.utils.SecurityUtils;
import com.hospital.equipment.modules.repairorder.entity.RepairOrder;
import com.hospital.equipment.modules.repairorder.service.RepairOrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报修工单控制器
 */
@RestController
@RequestMapping("/api/repair-order")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService orderService;

    /**
     * 提交报修单
     */
    @PostMapping
    public Result<RepairOrder> submit(
            @RequestParam Long deviceId,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile[] images) {
        Long reporterId = SecurityUtils.getCurrentUserId();
        List<String> imageUrls = new ArrayList<>();
        // 简化处理：如果没上传图片，传空列表即可
        // 实际图片上传走 /api/file/upload 接口，这里用 URL 列表
        RepairOrder order = orderService.submit(deviceId, reporterId, description, imageUrls);
        return Result.success(order);
    }

    /**
     * 批量图片 URL 版本的提交（更简洁）
     */
    @PostMapping("/v2")
    public Result<RepairOrder> submitV2(@RequestBody SubmitRequest req) {
        Long reporterId = SecurityUtils.getCurrentUserId();
        RepairOrder order = orderService.submit(req.deviceId, reporterId, req.description, req.images);
        return Result.success(order);
    }

    /**
     * 工单分页列表（engineer/admin）
     */
    @GetMapping("/page")
    public Result<PageResult<RepairOrder>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(orderService.page(pageNum, pageSize, status, keyword));
    }

    /**
     * 我的工单（nurse）
     */
    @GetMapping("/my")
    public Result<PageResult<RepairOrder>> myPage(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(defaultValue = "0") Integer status) {
        Long userId = SecurityUtils.getCurrentUserId();
        String role = SecurityUtils.getCurrentUserRole();

        if ("engineer".equals(role)) {
            return Result.success(orderService.assignedPage(pageNum, pageSize, userId, status));
        }
        return Result.success(orderService.myPage(pageNum, pageSize, userId, status));
    }

    /**
     * 工单详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(orderService.getDetail(id));
    }

    /**
     * 接单
     */
    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('engineer')")
    public Result<Void> accept(@PathVariable Long id) {
        Long engineerId = SecurityUtils.getCurrentUserId();
        orderService.accept(id, engineerId);
        return Result.success();
    }

    /**
     * 完工
     */
    @PutMapping("/{id}/finish")
    @PreAuthorize("hasRole('engineer')")
    public Result<Void> finish(@PathVariable Long id, @RequestBody FinishRequest req) {
        Long engineerId = SecurityUtils.getCurrentUserId();
        orderService.finish(id, engineerId, req.repairRemark, req.images);
        return Result.success();
    }

    /**
     * 评价
     */
    @PutMapping("/{id}/rate")
    public Result<Void> rate(@PathVariable Long id, @RequestBody RateRequest req) {
        Long nurseId = SecurityUtils.getCurrentUserId();
        orderService.rate(id, nurseId, req.rating, req.comment);
        return Result.success();
    }

    /**
     * 转派（admin）
     */
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> assign(@PathVariable Long id, @RequestBody AssignRequest req) {
        Long adminId = SecurityUtils.getCurrentUserId();
        orderService.assign(id, adminId, req.engineerId);
        return Result.success();
    }

    /**
     * 关闭（admin）
     */
    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> close(@PathVariable Long id, @RequestBody CloseRequest req) {
        Long adminId = SecurityUtils.getCurrentUserId();
        orderService.close(id, adminId, req.closeReason);
        return Result.success();
    }

    // ============ 请求体内部类 ============

    @Data
    public static class SubmitRequest {
        private Long deviceId;
        private String description;
        private List<String> images;
    }

    @Data
    public static class FinishRequest {
        private String repairRemark;
        private List<String> images;
    }

    @Data
    public static class RateRequest {
        private Integer rating;
        private String comment;
    }

    @Data
    public static class AssignRequest {
        private Long engineerId;
    }

    @Data
    public static class CloseRequest {
        private String closeReason;
    }
}
