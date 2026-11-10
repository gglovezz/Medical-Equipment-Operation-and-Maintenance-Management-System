package com.hospital.equipment.modules.file.controller;

import com.hospital.equipment.common.exception.BusinessException;
import com.hospital.equipment.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new BusinessException(400, "只支持图片文件");
        }

        // 校验大小（5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(400, "文件大小不能超过5MB");
        }

        // 创建日期目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        File dir = new File(uploadPath + dateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成文件名
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException(500, "文件保存失败");
        }

        String url = "/uploads/" + dateDir + "/" + fileName;
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("fileName", fileName);
        result.put("originalName", originalName);
        return Result.success(result);
    }
}
