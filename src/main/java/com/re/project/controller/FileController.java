package com.re.project.controller;

import com.re.project.dto.ApplicationDto;
import com.re.project.service.ApplicationService;
import com.re.project.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FileController {

    private final CloudinaryService cloudinaryService;
    private final ApplicationService applicationService;

    @PostMapping("/candidate/cv/upload")
    public ResponseEntity<String> uploadCV(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            // Check if PDF
            if (file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File must be PDF");
            }
            
            // Limit 15MB
            if (file.getSize() > 15 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size must be <= 15MB");
            }

            String url = cloudinaryService.uploadFile(file);
            return ResponseEntity.ok(url);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Lỗi kết nối đám mây: " + e.getMessage());
        }
    }
}
