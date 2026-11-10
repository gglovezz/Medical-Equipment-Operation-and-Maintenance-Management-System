package com.hospital.equipment.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.equipment.common.exception.BusinessException;
import com.hospital.equipment.modules.user.entity.SysUser;
import com.hospital.equipment.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 获取用户信息
     */
    public SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user != null) user.setPassword(null);
        return user;
    }

    /**
     * 修改密码
     */
    public void updatePassword(Long userId, String oldPwd, String newPwd) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new BusinessException(1003, "原密码错误");
        }
        if (newPwd.length() < 6 || newPwd.length() > 20) {
            throw new BusinessException(400, "新密码长度需在6-20位之间");
        }
        user.setPassword(passwordEncoder.encode(newPwd));
        userMapper.updateById(user);
    }

    /**
     * 获取所有工程师列表（用于派单）
     */
    public java.util.List<SysUser> listEngineers() {
        return userMapper.selectList(
                new QueryWrapper<SysUser>().eq("role", "engineer").eq("status", 1)
        );
    }
}
