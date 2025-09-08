package com.simada_backend.service.coach;

import com.simada_backend.dto.response.coach.CoachProfileDTO;
import com.simada_backend.model.Coach;
import com.simada_backend.model.User;
import com.simada_backend.repository.UserRepository;
import com.simada_backend.repository.coach.CoachRepository;
import com.simada_backend.service.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CoachProfileService {

    private final CoachRepository coachRepo;
    private final UserRepository userRepo;
    private final FileStorageService fileStorage;

    @Transactional
    public CoachProfileDTO getProfile(Long coachId) {
        Coach coach = coachRepo.findByIdWithUser(coachId)
                .orElseThrow(() -> new IllegalArgumentException("Coach não encontrado: " + coachId));
        return toDto(coach);
    }

    @Transactional
    public void updateProfile(Long coachId, CoachProfileDTO dto) {
        Coach coach = coachRepo.findByIdWithUser(coachId)
                .orElseThrow(() -> new IllegalArgumentException("Coach não encontrado: " + coachId));

        User u = coach.getUser();

        if (dto.name() != null) u.setName(dto.name());
        if (dto.email() != null) u.setEmail(dto.email());
        if (dto.phone() != null) u.setPhone(dto.phone());
        if (dto.gender() != null) u.setGender(dto.gender());
        if (dto.photoUrl() != null) u.setPhoto(dto.photoUrl());

        if (dto.team() != null) coach.setTeam(dto.team());

        userRepo.save(u);
        coachRepo.save(coach);
    }

    @Transactional
    public String uploadAvatar(Long coachId, MultipartFile file) throws Exception {
        var coach = coachRepo.findByIdWithUser(coachId)
                .orElseThrow(() -> new IllegalArgumentException("Coach não encontrado: " + coachId));

        String publicUrl = fileStorage.storeCoachAvatar(coachId, file);
        coach.getUser().setPhoto(publicUrl);
        userRepo.save(coach.getUser());
        return publicUrl;
    }

    private CoachProfileDTO toDto(Coach c) {
        User u = c.getUser();
        return new CoachProfileDTO(
                c.getId(),
                u.getName(),
                u.getEmail(),
                u.getGender(),
                c.getTeam(),
                u.getPhone(),
                u.getPhoto()
        );
    }
}
