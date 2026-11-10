package com.hospital.equipment.modules.inspect.controller;

import com.hospital.equipment.common.result.PageResult;
import com.hospital.equipment.common.result.Result;
import com.hospital.equipment.common.utils.SecurityUtils;
import com.hospital.equipment.modules.inspect.entity.InspectTask;
import com.hospital.equipment.modules.inspect.service.InspectService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 巡检任务控制器
 */
@RestController
@RequestMapping("/api/inspect-task")
@RequiredArgsConstructor
public class InspectController {

    private final InspectService inspectService;

    @GetMapping("/my")
    public Result<PageResult<InspectTask>> myTasks(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(defaultValue = "0") Integer status) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(inspectService.myTasks(pageNum, pageSize, userId, status));
    }

    @PutMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id, @RequestBody SubmitRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        inspectService.submit(id, userId, req.result, req.remark, req.images);
        return Result.success();
    }

    /**
     * 管理员手动触发生成巡检任务
     */
    @PostMapping("/generate")
    public Result<Integer> generate() {
        return Result.success(inspectService.generateDailyTasks());
    }

    @Data
    public static class SubmitRequest {
        private Integer result;
        private String remark;
        private List<String> images;
    }
}
