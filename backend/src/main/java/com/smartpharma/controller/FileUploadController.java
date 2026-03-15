package com.smartpharma.controller;

import com.smartpharma.common.Result;
import com.smartpharma.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传控制器
 * 提供图片上传功能
 */
@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * 上传图片文件
     * @param file 图片文件
     * @return 图片访问URL
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.fail("请选择要上传的文件");
            }

            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return Result.fail("文件格式不支持");
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!fileExtension.matches("jpg|jpeg|png|gif|webp|bmp")) {
                return Result.fail("只支持图片文件格式: jpg, jpeg, png, gif, webp, bmp");
            }

            // 验证文件大小（最大10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return Result.fail("文件大小不能超过10MB");
            }

            // 存储文件并获取URL
            String fileUrl = fileStorageService.store(file);
            log.info("图片上传成功: {} -> {}", originalFilename, fileUrl);
            
            return Result.ok(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("文件上传失败", e);
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败");
        }
    }

    /**
     * 删除图片文件
     * @param fileUrl 文件URL
     * @return 删除结果
     */
    @DeleteMapping("/image")
    public Result<Void> deleteImage(@RequestParam String fileUrl) {
        try {
            // 从URL中提取文件名
            String fileName = extractFileNameFromUrl(fileUrl);
            if (fileName == null || fileName.isEmpty()) {
                return Result.fail("无效的文件URL");
            }

            boolean deleted = fileStorageService.delete(fileName);
            if (deleted) {
                log.info("文件删除成功: {}", fileName);
                return Result.ok();
            } else {
                return Result.fail("文件删除失败，文件可能不存在");
            }
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.fail("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 从URL中提取文件名
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        
        // 从URL中提取文件名，例如：http://localhost:8080/api/uploads/abc123.jpg -> abc123.jpg
        String[] parts = fileUrl.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return null;
    }
}