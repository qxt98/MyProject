package com.smartpharma.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 文件存储服务
 * 负责处理文件上传、存储和URL生成
 */
@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 存储上传的文件
     * @param file 上传的文件
     * @return 文件的访问URL
     * @throws IOException 文件存储异常
     */
    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        if (!isImageFile(fileExtension)) {
            throw new RuntimeException("只支持图片文件格式: jpg, jpeg, png, gif, webp");
        }

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + "." + fileExtension;
        
        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 保存文件
        Path targetLocation = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        log.info("文件已保存: {}", targetLocation.toString());

        // 生成访问URL
        return generateFileUrl(fileName);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 检查是否为图片文件
     */
    private boolean isImageFile(String extension) {
        return extension.matches("jpg|jpeg|png|gif|webp|bmp");
    }

    /**
     * 生成文件访问URL
     */
    private String generateFileUrl(String fileName) {
        // 本地开发环境URL
        String baseUrl = "http://localhost:" + serverPort + contextPath;
        
        // 文件访问路径
        String filePath = "/uploads/" + fileName;
        
        return baseUrl + filePath;
    }

    /**
     * 删除文件
     */
    public boolean delete(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("删除文件失败: {}", fileName, e);
            return false;
        }
    }
}