package com.rainbowmaodie.zjutbookingsystem.controller;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Slf4j
public class UploadController {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.base-url:http://localhost:8080/uploads/}")
    private String baseUrl;

    /**
     * Upload Venue Image (1:1 ratio, compress to 800x800)
     */
    @PostMapping("/venue")
    public ResponseEntity<Map<String, Object>> uploadVenueImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(error("File is empty"));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return ResponseEntity.badRequest().body(error("Invalid filename"));
        }

        try {
            File dir = new File(uploadPath + "venues/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + ext;
            File dest = new File(dir, fileName);

            // Using Thumbnailator to center-crop if needed (though frontend should do it)
            // and resize to 800x800
            Thumbnails.of(file.getInputStream())
                    .size(800, 800)
                    .keepAspectRatio(true) // We expect 1:1 from frontend
                    .outputQuality(0.8)
                    .toFile(dest);

            Map<String, Object> data = new HashMap<>();
            data.put("url", baseUrl + "venues/" + fileName);
            data.put("path", "venues/" + fileName);
            
            return ResponseEntity.ok(data);

        } catch (IOException e) {
            log.error("Failed to upload venue image", e);
            return ResponseEntity.status(500).body(error("Server error: " + e.getMessage()));
        }
    }

    /**
     * Upload signature image (PNG, 300x100)
     */
    @PostMapping("/signature")
    public ResponseEntity<Map<String, Object>> uploadSignature(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(error("File is empty"));
        }

        // 1. Basic validation
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".png")) {
            return ResponseEntity.badRequest().body(error("Only PNG files are allowed"));
        }

        if (file.getSize() > 1024 * 1024) {
            return ResponseEntity.badRequest().body(error("File size exceeds 1MB"));
        }

        try {
            // 2. Ensure directory exists
            File dir = new File(uploadPath + "signatures/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 3. Generate unique filename
            String fileName = UUID.randomUUID().toString() + ".png";
            File dest = new File(dir, fileName);

            // 4. Using Thumbnailator to force 300x100 dimensions and compress
            Thumbnails.of(file.getInputStream())
                    .forceSize(300, 100)
                    .outputFormat("png")
                    .toFile(dest);

            // 5. Return full URL
            Map<String, Object> data = new HashMap<>();
            data.put("url", baseUrl + "signatures/" + fileName);
            data.put("path", "signatures/" + fileName); // For DB storage
            
            return ResponseEntity.ok(data);

        } catch (IOException e) {
            log.error("Failed to upload signature", e);
            return ResponseEntity.status(500).body(error("Server error: " + e.getMessage()));
        }
    }

    /**
     * Delete an uploaded file
     * @param path the relative path (e.g., "venues/uuid.jpg" or "signatures/uuid.png")
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam("path") String path) {
        if (path == null || path.isEmpty()) {
            return ResponseEntity.badRequest().body(error("Path is empty"));
        }

        // Security check: path must not contain ".." to avoid directory traversal
        if (path.contains("..")) {
            return ResponseEntity.badRequest().body(error("Invalid path"));
        }

        File file = new File(uploadPath, path);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Map<String, Object> data = new HashMap<>();
                data.put("message", "File deleted successfully");
                return ResponseEntity.ok(data);
            } else {
                return ResponseEntity.status(500).body(error("Failed to delete file"));
            }
        } else {
            return ResponseEntity.badRequest().body(error("File does not exist"));
        }
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> res = new HashMap<>();
        res.put("message", msg);
        res.put("error", true);
        return res;
    }
}
