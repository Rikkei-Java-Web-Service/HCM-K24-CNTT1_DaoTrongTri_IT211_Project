package com.re.project.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final String folder;

    public CloudinaryService(
            @Value("${cloudinary.cloud_name:demo}") String cloudName,
            @Value("${cloudinary.api_key:key}") String apiKey,
            @Value("${cloudinary.api_secret:secret}") String apiSecret,
            @Value("${cloudinary.folder:cv_uploads}") String folder) {

        this.folder = folder;
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    public String uploadFile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", folder, "resource_type", "raw"));
        return uploadResult.get("secure_url").toString();
    }
}
