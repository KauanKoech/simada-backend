package com.simada_backend.service.coach;

import com.simada_backend.api.error.BusinessException;
import com.simada_backend.api.error.ErrorCode;
import com.simada_backend.dto.response.coach.CoachProfileDTO;
import com.simada_backend.events.CoachTransferCompletedEvent;
import com.simada_backend.model.Coach;
import com.simada_backend.model.User;
import com.simada_backend.repository.UserRepository;
import com.simada_backend.repository.coach.CoachRepository;
import com.simada_backend.service.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoachProfileService {

    private final CoachRepository coachRepo;
    private final UserRepository userRepo;
    private final FileStorageService fileStorage;
    private final ApplicationEventPublisher events;

    @Transactional
    public CoachProfileDTO getProfile(Long userId) {
        Coach coach = coachRepo.findByIdWithUser(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Coach not found."
                ));
        return toDto(coach);
    }

    @Transactional
    public void updateProfile(Long coachId, CoachProfileDTO dto) {
        Coach coach = coachRepo.findByIdWithUser(coachId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Coach not found."
                ));

        User u = coach.getUser();

        if (dto.name() != null) {
            u.setName(dto.name());
            coach.setName(dto.name());
        }
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
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Coach not found."
                ));

        String publicUrl = fileStorage.storeCoachAvatar(coachId, file);
        coach.getUser().setPhoto(publicUrl);
        userRepo.save(coach.getUser());
        return publicUrl;
    }

    @Transactional
    public void deleteOrTransferCoachAccount(Long sourceCoachId, String transferToEmail) {
        // NÃO carrega Coach como entidade. Só pega os dados necessários:
        Long sourceUserId = coachRepo.findUserIdByCoachId(sourceCoachId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Coach not found."
                ));

        String sourceCoachName = coachRepo.findUserNameById(sourceUserId)
                .orElse("Coach " + sourceCoachId);

        // ===== TRANSFERÊNCIA =====
        if (StringUtils.hasText(transferToEmail)) {
            String email = transferToEmail.trim();

            Long destUserId = coachRepo.findUserIdByEmail(email)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.RESOURCE_NOT_FOUND,
                            HttpStatus.NOT_FOUND,
                            "Destination user not found." + email
                    ));

            if (destUserId.equals(sourceUserId)) {
                throw new IllegalArgumentException("Destination email is the same as the source coach.");
            }

            // garante coach destino via INSERT nativo (sem JPA)
            coachRepo.ensureCoachExistsForUser(destUserId);
            Long destCoachId = coachRepo.findCoachIdByUserId(destUserId)
                    .orElseThrow(() -> new IllegalStateException("Failed to create/retrieve destination Coach for " + email));

            // reassign TUDO por coachId (NÃO por userId)
            coachRepo.reassignAthlete(sourceCoachId, destCoachId);
            coachRepo.reassignAthleteInvite(sourceCoachId, destCoachId);
            coachRepo.reassignSession(sourceCoachId, destCoachId);
            coachRepo.reassignPsychoAlert(sourceCoachId, destCoachId);
            coachRepo.reassignPsychoRiskScore(sourceCoachId, destCoachId);
            coachRepo.reassignPsychoFormInvite(sourceCoachId, destCoachId);

            // Remove coach origem e depois o user origem (por id direto)
            coachRepo.deleteCoachRow(sourceCoachId);
            userRepo.deleteById(sourceUserId);

            // e-mail pós-commit
            String destCoachName = coachRepo.findUserNameById(destUserId).orElse("Coach " + destCoachId);
            events.publishEvent(new CoachTransferCompletedEvent(
                    sourceCoachId, sourceCoachName, destCoachId, email, destCoachName
            ));
            return;
        }

        // ===== EXCLUSÃO =====
        // snapshot dos users dos atletas ANTES
        List<Long> athleteUserIds = coachRepo.findAthleteUserIdsByCoach(sourceCoachId);

        // psico
        coachRepo.deletePsychoAlertsByCoach(sourceCoachId);
        coachRepo.deletePsychoRiskScoresByCoach(sourceCoachId);
        coachRepo.deletePsychoFormAnswersByCoach(sourceCoachId);
        coachRepo.deletePsychoFormInvitesByCoach(sourceCoachId);

        // sessões/métricas
        coachRepo.deleteMetricsByCoachViaSessions(sourceCoachId);
        coachRepo.deleteSessionsByCoach(sourceCoachId);

        // atletas + extras + convites
        coachRepo.deleteAthleteExtrasByCoach(sourceCoachId);
        coachRepo.deleteAthleteInvitesByCoach(sourceCoachId);
        coachRepo.deleteAthletesByCoach(sourceCoachId);

        // users dos atletas (agora pode apagar)
        if (!athleteUserIds.isEmpty()) {
            userRepo.deleteAllByIdInBatch(athleteUserIds);
        }

        // user do coach e linha do coach
        userRepo.deleteById(sourceUserId);
        coachRepo.deleteCoachRow(sourceCoachId);
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
