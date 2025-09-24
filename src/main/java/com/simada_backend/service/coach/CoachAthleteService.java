package com.simada_backend.service.coach;

import com.simada_backend.api.error.BusinessException;
import com.simada_backend.api.error.ErrorCode;
import com.simada_backend.dto.request.athlete.UpdateAthleteRequest;
import com.simada_backend.dto.response.athlete.AthleteDetailDTO;
import com.simada_backend.dto.response.athlete.AthleteExtraDTO;
import com.simada_backend.model.User;
import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.athlete.AthleteExtra;
import com.simada_backend.repository.UserRepository;
import com.simada_backend.repository.athlete.AthleteExtraRepository;
import com.simada_backend.repository.athlete.AthleteInviteRepository;
import com.simada_backend.repository.athlete.AthleteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CoachAthleteService {

    private final AthleteRepository athleteRepository;
    private final AthleteExtraRepository extraRepository;
    private final UserRepository userRepository;
    private final AthleteInviteRepository inviteRepository;

    @Transactional
    public AthleteDetailDTO getAthlete(Long coachId, Long athleteId) {
        Athlete athlete = athleteRepository.findByIdAndCoach_Id(athleteId, coachId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Athlete not found."
                ));

        User u = athlete.getUser();
        AthleteExtra ex = athlete.getExtra();

        return new AthleteDetailDTO(
                athlete.getId(),
                athlete.getName(),
                u != null ? u.getEmail() : null,
                u != null ? u.getPhone() : null,
                u != null && u.getBirthDate() != null ? u.getBirthDate().toString() : null,
                u != null ? u.getPassword() : null,
                athlete.getJerseyNumber() != null ? String.valueOf(athlete.getJerseyNumber()) : null,
                athlete.getPosition(),
                ex == null ? null : new AthleteExtraDTO(
                        ex.getHeightCm(), ex.getWeightKg(), ex.getLeanMassKg(), ex.getFatMassKg(),
                        ex.getBodyFatPct(), ex.getDominantFoot(), ex.getNationality(), ex.getInjuryStatus()
                )
        );
    }

    @Transactional
    public AthleteDetailDTO updateAthlete(Long coachId, Long athleteId, UpdateAthleteRequest req) {
        Athlete athlete = athleteRepository.findByIdAndCoach_Id(athleteId, coachId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Athlete not found."
                ));

        if (req.getName() != null) athlete.setName(req.getName());
        if (req.getPosition() != null) athlete.setPosition(req.getPosition());

        User u = athlete.getUser();
        if (u != null) {
            if (req.getEmail() != null) u.setEmail(req.getEmail());
            if (req.getPhone() != null) u.setPhone(req.getPhone());
            if (req.getBirth() != null && !req.getBirth().isBlank()) {
                try {
                    u.setBirthDate(LocalDate.parse(req.getBirth()));
                } catch (Exception e) {
                    throw new BusinessException(
                            ErrorCode.VALIDATION_ERROR,
                            HttpStatus.BAD_REQUEST,
                            "Birth format invalid (use YYYY-MM-DD)."
                    );
                }
            }
            userRepository.save(u);
        }

        // Extra
        if (req.getExtra() != null) {
            AthleteExtra ex = extraRepository.findByAthlete_Id(athleteId).orElseGet(() -> {
                AthleteExtra novo = new AthleteExtra();
                novo.setAthlete(athlete);
                return novo;
            });

            var e = req.getExtra();
            ex.setHeightCm(e.height_cm());
            ex.setWeightKg(e.weight_kg());
            ex.setLeanMassKg(e.lean_mass_kg());
            ex.setFatMassKg(e.fat_mass_kg());
            ex.setBodyFatPct(e.body_fat_pct());
            ex.setDominantFoot(e.dominant_foot());
            ex.setNationality(e.nationality());
            ex.setInjuryStatus(e.injury_status());

            extraRepository.save(ex);
            athlete.setExtra(ex);
        }

        athleteRepository.save(athlete);
        return getAthlete(coachId, athleteId);
    }

    @Transactional
    public void deleteAthlete(Long athleteId) {
        Athlete a = athleteRepository.findById(athleteId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Athlete not found."
                ));

        athleteRepository.deleteById(athleteId);
        extraRepository.deleteById(athleteId);
        inviteRepository.deleteByEmail(a.getUser().getEmail());
        userRepository.delete(a.getUser());
    }
}
