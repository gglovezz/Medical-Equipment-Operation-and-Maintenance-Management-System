package com.hospital.equipment.modules.stats.controller;

import com.hospital.equipment.common.result.Result;
import com.hospital.equipment.modules.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统计报表控制器
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('admin')")
    public Result<Map<String, Object>> overview(
            @RequestParam(required = false) String month) {
        return Result.success(statsService.overview(month));
    }
}
