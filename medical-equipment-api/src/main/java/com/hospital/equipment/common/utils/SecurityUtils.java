package com.hospital.equipment.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * 安全上下文工具类 - 获取当前登录用户信息
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     */
    @SuppressWarnings("unchecked")
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("用户未登录");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Map) {
            return ((Map<String, Object>) principal).get("userId") != null
                    ? Long.valueOf(((Map<String, Object>) principal).get("userId").toString())
                    : null;
        }
        return null;
    }

    /**
     * 获取当前用户角色
     */
    @SuppressWarnings("unchecked")
    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof Map) {
            return (String) ((Map<String, Object>) principal).get("role");
        }
        return null;
    }

    /**
     * 获取完整用户信息 Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return new HashMap<>();
        Object principal = auth.getPrincipal();
        if (principal instanceof Map) {
            return (Map<String, Object>) principal;
        }
        return new HashMap<>();
    }
}
