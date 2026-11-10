package com.hospital.equipment.modules.user.service;

import com.hospital.equipment.common.exception.BusinessException;
import com.hospital.equipment.common.utils.JwtUtil;
import com.hospital.equipment.modules.user.entity.SysUser;
import com.hospital.equipment.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        SysUser user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysUser>()
                        .eq("username", username)
        );

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(1001, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(1002, "账号已禁用，请联系管理员");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("expiresIn", jwtUtil.getExpiration() / 1000);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("dept", user.getDept());
        result.put("userInfo", userInfo);

        return result;
    }
}
