package com.hospital.equipment.modules.user.controller;

import com.hospital.equipment.common.result.Result;
import com.hospital.equipment.common.utils.SecurityUtils;
import com.hospital.equipment.modules.user.entity.SysUser;
import com.hospital.equipment.modules.user.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public Result<SysUser> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.getById(userId));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody PasswordUpdateRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updatePassword(userId, req.oldPassword, req.newPassword);
        return Result.success();
    }

    @GetMapping("/engineers")
    @PreAuthorize("hasAnyRole('admin')")
    public Result<List<SysUser>> listEngineers() {
        return Result.success(userService.listEngineers());
    }

    @Data
    public static class PasswordUpdateRequest {
        private String oldPassword;
        private String newPassword;
    }
}
