package com.simada_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.util.Base64;
import java.util.Set;


@Service
public class FileStorageService {

    // até ~300 KB por imagem (ajuste conforme sua realidade)
    private static final long MAX_BYTES = 300_000;
    private static final Set<String> ALLOWED = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp"
    );

    public String storeCoachAvatar(Long coachId, MultipartFile file) throws Exception {
        return toDataUrl(file);
    }

    public String storeAthleteAvatar(Long athleteId, MultipartFile file) throws Exception {
        return toDataUrl(file);
    }

    private String toDataUrl(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de imagem vazio.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de imagem não suportado: " + contentType);
        }

        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Imagem acima do limite de " + (MAX_BYTES / 1024) + " KB.");
        }

        // Valida minimamente se é imagem (protege contra upload malicioso)
        try (var in = file.getInputStream()) {
            if (ImageIO.read(in) == null) {
                throw new IllegalArgumentException("Arquivo não é uma imagem válida.");
            }
        }

        byte[] bytes = file.getBytes();
        String b64 = Base64.getEncoder().encodeToString(bytes);
        return "data:" + contentType + ";base64," + b64;
    }
}
