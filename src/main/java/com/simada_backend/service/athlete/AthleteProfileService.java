package com.simada_backend.service.athlete;

import com.simada_backend.api.error.BusinessException;
import com.simada_backend.api.error.ErrorCode;
import com.simada_backend.dto.response.athlete.*;
import com.simada_backend.dto.request.athlete.*;
import com.simada_backend.repository.UserRepository;
import com.simada_backend.repository.alert.PsychoAlertRepository;
import com.simada_backend.repository.alert.TrainingLoadAlertRepository;
import com.simada_backend.repository.athlete.AthleteExtraRepository;
import com.simada_backend.repository.athlete.AthletePerformanceSnapshotRepository;
import com.simada_backend.repository.athlete.AthleteProfileRepository;
import com.simada_backend.repository.athlete.AthleteRepository;
import com.simada_backend.repository.loadCalc.SessionLoadRepo;
import com.simada_backend.repository.psycho.PsychoFormAnswerRepository;
import com.simada_backend.repository.psycho.PsychoFormInviteRepository;
import com.simada_backend.repository.psycho.PsychoRiskScoreRepository;
import com.simada_backend.repository.recommendation.PerfRecommendationRepository;
import com.simada_backend.repository.recommendation.PsyRecommendationRepository;
import com.simada_backend.repository.session.MetricsRepository;
import com.simada_backend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AthleteProfileService {

    private final AthleteRepository athleteRepository;
    private final UserRepository userRepo;
    private final AthleteProfileRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorage;
    private final MetricsRepository metricsRepository;
    private final PerfRecommendationRepository perfRecRepo;
    private final PsyRecommendationRepository psyRecRepo;
    private final PsychoRiskScoreRepository riskRepo;
    private final PsychoAlertRepository psychoAlertRepo;
    private final PsychoFormAnswerRepository formAnswerRepo;
    private final PsychoFormInviteRepository formInviteRepo;
    private final SessionLoadRepo sessionLoadRepo;
    private final AthletePerformanceSnapshotRepository snapshotRepo;
    private final AthleteExtraRepository athleteExtraRepo;
    private final TrainingLoadAlertRepository loadAlertRepo;

    private final Path uploadDir = Paths.get("uploads/avatars");

    @Transactional(readOnly = true)
    public AthleteProfileDTO getProfile(Long athleteId) {
        var p = repo.findProfile(athleteId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Athlete not found."
                ));

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
        if (userId == null) throw new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "User not linked to athlete."
        );

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
    public String uploadAvatar(Long athleteId, MultipartFile file) {
        try {
            var coach = athleteRepository.findById(athleteId)
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.RESOURCE_NOT_FOUND,
                            HttpStatus.NOT_FOUND,
                            "Athlete not found."
                    ));


            String publicUrl = fileStorage.storeAthleteAvatar(athleteId, file);
            coach.getUser().setPhoto(publicUrl);
            userRepo.save(coach.getUser());
            return publicUrl;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    HttpStatus.BAD_REQUEST,
                    "Failed to upload avatar."
            );
        }
    }

    @Transactional
    public void changePassword(Long athleteId, UpdatePasswordRequest req) {
        Long userId = repo.findUserIdByAthleteId(athleteId);
        if (userId == null) throw new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                "User not linked to athlete."
        );

        String currentHash = repo.getPasswordHash(userId);
        if (currentHash == null || !passwordEncoder.matches(req.currentPassword(), currentHash)) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    HttpStatus.BAD_REQUEST,
                    "Current password is invalid."
            );
        }
        String newHash = passwordEncoder.encode(req.newPassword());
        repo.setPasswordHash(userId, newHash);
    }

    @Transactional
    public void deleteAthleteCascade(Long athleteId) {
        //Verifica existência
        athleteRepository.findById(athleteId).orElseThrow(() ->
                new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Athlete not found: id=" + athleteId
                )
        );

        psychoAlertRepo.deleteByAnswerAthleteId(athleteId);
        riskRepo.deleteByAnswerAthleteId(athleteId);

        formAnswerRepo.deleteByAthleteId(athleteId);

        perfRecRepo.deleteByAthleteId(athleteId);
        psyRecRepo.deleteByAthleteId(athleteId);
        formInviteRepo.deleteByAthleteId(athleteId);
        sessionLoadRepo.deleteByAthleteId(athleteId);
        snapshotRepo.deleteByAthleteId(athleteId);
        athleteExtraRepo.deleteByAthleteId(athleteId);
        loadAlertRepo.deleteByAthleteId(athleteId);
        metricsRepository.deleteByAthleteId(athleteId);

        athleteRepository.deleteById(athleteId);
    }

    private String OptionalExtension(String original) {
        if (original == null) return "";
        int i = original.lastIndexOf('.');
        return (i >= 0 ? original.substring(i) : "");
    }
}
