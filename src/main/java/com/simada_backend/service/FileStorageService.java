package com.simada_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path root = Paths.get("uploads/avatars");

    @Value("${app.public-base-url}")
    private String publicBaseUrl; // ex.: http://localhost:8080

    public FileStorageService() throws Exception {
        Files.createDirectories(root);
    }

    public String storeCoachAvatar(Long coachId, MultipartFile file) throws Exception {
        String ext = getExtension(file.getOriginalFilename());
        String filename = "coach-" + coachId + "-" + Instant.now().toEpochMilli()
                + "-" + UUID.randomUUID() + (ext.isBlank() ? "" : "." + ext);
        Path target = root.resolve(filename).normalize();

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // URL ABSOLUTA que o front pode usar diretamente no <img src="...">
        return publicBaseUrl + "/static/avatars/" + filename;
    }

    private String getExtension(String original) {
        if (!StringUtils.hasText(original)) return "";
        int i = original.lastIndexOf('.');
        return (i > -1 && i < original.length() - 1) ? original.substring(i + 1) : "";
    }
}
