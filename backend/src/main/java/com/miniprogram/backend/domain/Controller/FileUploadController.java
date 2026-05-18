package com.miniprogram.backend.domain.Controller;

import com.miniprogram.backend.common.ApiResponse;
import com.miniprogram.backend.common.RequireAdmin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传控制器
 * 提供图片上传功能，保存到本地文件夹
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
public class FileUploadController {
    
    @Value("${file.upload.path}")
    private String uploadPath;
    
    @Value("${file.upload.max-size:5242880}")
    private long maxFileSize;
    
    /**
     * 上传图片 - 需要管理员权限
     * @param file 图片文件
     * @param type 图片类型：products-商品图片, banners-轮播图
     * @return 图片访问URL
     */
    @RequireAdmin
    @PostMapping("/upload")
    public ApiResponse<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "products") String type) {
        
        log.info("Uploading image - fileName: {}, size: {}, type: {}", 
                 file.getOriginalFilename(), file.getSize(), type);
        
        // 验证文件
        if (file.isEmpty()) {
            return ApiResponse.error(400, "File cannot be empty");
        }
        
        if (file.getSize() > maxFileSize) {
            return ApiResponse.error(400, "File size exceeds limit: " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.error(400, "Only image files are allowed");
        }
        
        try {
            // 创建目录结构：/app/pics/{type}/
            String subDir = type.equals("banners") ? "banners" : "products";
            Path dirPath = Paths.get(uploadPath, subDir);
            Files.createDirectories(dirPath);
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = dirPath.resolve(newFilename);
            file.transferTo(filePath.toFile());
            
            log.info("Image uploaded successfully - path: {}", filePath);
            
            // 返回访问URL（相对路径）
            String imageUrl = "/pics/" + subDir + "/" + newFilename;
            
            return ApiResponse.success(imageUrl);
            
        } catch (IOException e) {
            log.error("Failed to upload image", e);
            return ApiResponse.error(500, "Failed to upload image: " + e.getMessage());
        }
    }
    
    /**
     * 删除图片 - 需要管理员权限
     * @param imageUrl 图片URL（如：/pics/products/xxx.jpg）
     */
    @RequireAdmin
    @DeleteMapping("/delete")
    public ApiResponse<Void> deleteImage(@RequestParam("imageUrl") String imageUrl) {
        log.info("Deleting image - url: {}", imageUrl);
        
        try {
            // 从URL提取文件路径
            // /pics/products/xxx.jpg -> products/xxx.jpg
            String relativePath = imageUrl.replaceFirst("^/pics/", "");
            Path filePath = Paths.get(uploadPath, relativePath);
            
            File file = filePath.toFile();
            if (!file.exists()) {
                return ApiResponse.error(404, "Image not found");
            }
            
            // 删除文件
            Files.delete(filePath);
            log.info("Image deleted successfully - path: {}", filePath);
            
            return ApiResponse.success(null);
            
        } catch (IOException e) {
            log.error("Failed to delete image", e);
            return ApiResponse.error(500, "Failed to delete image: " + e.getMessage());
        }
    }
}
