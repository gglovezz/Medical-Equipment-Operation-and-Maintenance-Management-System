package com.hospital.equipment.modules.user.controller;

import com.hospital.equipment.common.result.Result;
import com.hospital.equipment.modules.user.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> data = authService.login(request.username, request.password);
        return Result.success(data);
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
