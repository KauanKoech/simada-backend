package com.simada_backend.service.athlete;

import com.simada_backend.dto.response.athlete.*;
import com.simada_backend.dto.request.athlete.*;
import com.simada_backend.repository.athlete.AthleteProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AthleteProfileService {

    private final AthleteProfileRepository repo;
    private final PasswordEncoder passwordEncoder;

    private final Path uploadDir = Paths.get("uploads/avatars");

    @Transactional(readOnly = true)
    public AthleteProfileDTO getProfile(Long athleteId) {
        var p = repo.findProfile(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found: " + athleteId));
        return new AthleteProfileDTO(
                p.getId(),
                p.getName(),
                p.getEmail(),
                p.getGender(),
                p.getPhone(),
                p.getNationality(),
                p.getPhotoUrl()
        );
    }

    @Transactional
    public void updateProfile(Long athleteId, UpdateAthleteProfileRequest req) {
        Long userId = repo.findUserIdByAthleteId(athleteId);
        if (userId == null) throw new IllegalArgumentException("User not linked to athlete: " + athleteId);

        // atualiza “nome esportivo” do athlete se veio
        if (req.name() != null && !req.name().isBlank()) {
            repo.updateAthleteName(athleteId, req.name());
        }

        // atualiza dados básicos do user (COALESCE mantém valor atual se vier null)
        repo.updateUserBasics(userId, req.name(), req.email(), req.gender(), req.phone());

        // nationality (upsert) se veio
        if (req.nationality() != null) {
            repo.upsertNationality(athleteId, req.nationality());
        }
    }

    @Transactional
    public UploadAvatarResponse uploadAvatar(Long athleteId, MultipartFile file) {
        try {
            Long userId = repo.findUserIdByAthleteId(athleteId);
            if (userId == null) throw new IllegalArgumentException("User not linked to athlete: " + athleteId);

            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);

            String ext = OptionalExtension(file.getOriginalFilename());
            String filename = "athlete-" + athleteId + "-" + UUID.randomUUID() + ext;
            Path target = uploadDir.resolve(filename);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // URL pública (ajuste se você expõe estáticos por outro caminho)
            String publicUrl = "/uploads/avatars/" + filename;

            repo.updateUserPhoto(userId, publicUrl);
            return new UploadAvatarResponse(publicUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload avatar", e);
        }
    }

    @Transactional
    public void changePassword(Long athleteId, UpdatePasswordRequest req) {
        Long userId = repo.findUserIdByAthleteId(athleteId);
        if (userId == null) throw new IllegalArgumentException("User not linked to athlete: " + athleteId);

        String currentHash = repo.getPasswordHash(userId);
        if (currentHash == null || !passwordEncoder.matches(req.currentPassword(), currentHash)) {
            throw new IllegalArgumentException("Current password is invalid");
        }
        String newHash = passwordEncoder.encode(req.newPassword());
        repo.setPasswordHash(userId, newHash);
    }

    // util
    private String OptionalExtension(String original) {
        if (original == null) return "";
        int i = original.lastIndexOf('.');
        return (i >= 0 ? original.substring(i) : "");
    }
}
