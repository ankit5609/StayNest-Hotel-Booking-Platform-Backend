package com.cybernode.projects.HotelBookingApp.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cybernode.projects.HotelBookingApp.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadServiceImpl implements ImageUploadService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only JPEG, PNG, and WEBP image formats are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Image file size must be less than 5MB");
        }

        boolean isCloudinaryConfigured = cloudName != null && !cloudName.trim().isEmpty() &&
                                          apiKey != null && !apiKey.trim().isEmpty() &&
                                          apiSecret != null && !apiSecret.trim().isEmpty();

        if (!isCloudinaryConfigured) {
            log.info("Cloudinary credentials are not configured. Falling back to local file upload.");
            try {
                java.io.File uploadDir = new java.io.File("uploads");
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                String originalFilename = file.getOriginalFilename();
                String extension = ".jpg";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }

                String newFilename = UUID.randomUUID().toString() + extension;
                java.nio.file.Path filePath = uploadDir.toPath().resolve(newFilename);
                java.nio.file.Files.write(filePath, file.getBytes());

                return "http://localhost:8080/uploads/" + newFilename;
            } catch (IOException e) {
                log.error("Local file upload failed", e);
                throw new RuntimeException("Failed to save file locally: " + e.getMessage());
            }
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            log.error("Cloudinary image upload failed", e);
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }
}
