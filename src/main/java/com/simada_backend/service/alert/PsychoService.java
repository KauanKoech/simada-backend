package com.simada_backend.service.alert;

import com.simada_backend.dto.response.alert.PsychoAlertDTO;
import com.simada_backend.dto.response.psycho.PsychoAnswerAthleteDTO;
import com.simada_backend.model.psycho.*;
import com.simada_backend.repository.alert.PsychoAlertRepository;
import com.simada_backend.repository.psycho.PsychoRiskScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PsychoService {

    private final PsychoRiskScoreRepository riskRepo;
    private final PsychoAlertRepository alertRepo;

    public PsychoService(PsychoRiskScoreRepository riskRepo, PsychoAlertRepository alertRepo) {
        this.riskRepo = Objects.requireNonNull(riskRepo);
        this.alertRepo = Objects.requireNonNull(alertRepo);
    }

    @Transactional
    public PsychoRiskScore processAnswer(PsychoFormInvite invite, PsychoFormAnswer answer) {
        int srpe = nz(answer.getSrpe());
        int fatigue = nz(answer.getFatigue());
        int soreness = nz(answer.getSoreness());
        int moodInv = invert10(answer.getMood());
        int energyInv = invert10(answer.getEnergy());

        int total = srpe + fatigue + soreness + moodInv + energyInv;
        RiskLevel level = mapLevel(total);

        // 1) Save risk score (now using ENTITIES instead of raw IDs)
        PsychoRiskScore prs = new PsychoRiskScore();
        prs.setCoach(invite.getIdCoach());       // Coach entity
        prs.setAthlete(invite.getIdAthlete());   // Athlete entity
        prs.setSession(invite.getIdSession());   // Session entity
        prs.setAnswer(answer);                   // PsychoFormAnswer entity
        prs.setRiskLevel(level);
        prs.setTotalScore(total);
        prs.setCreatedAt(LocalDateTime.now());
        riskRepo.save(prs);

        // 2) Create alert only for MODERATE/HIGH
        if (level == RiskLevel.MODERATE || level == RiskLevel.HIGH) {
            PsychoAlert alert = new PsychoAlert();
            alert.setCoach(invite.getIdCoach());
            alert.setAthlete(invite.getIdAthlete());
            alert.setSession(invite.getIdSession());
            alert.setAnswer(answer);
            alert.setRuleCode(level == RiskLevel.HIGH ? "PSYCHO_HIGH" : "PSYCHO_MODERATE");
            alert.setSeverity(level == RiskLevel.HIGH ? "CRITICAL" : "WARN");

            String msg = "Score: " + total + " (" + (level == RiskLevel.HIGH ? "High risk" : "Moderate risk") + "). "
                    + "Details — sRPE: " + srpe
                    + ", Fatigue: " + fatigue
                    + ", Soreness: " + soreness
                    + ", Mood(inv): " + moodInv
                    + ", Energy(inv): " + energyInv + ".";
            alert.setMessage(msg);
            alert.setCreatedAt(LocalDateTime.now());
            alertRepo.save(alert);
        }

        return prs;
    }

    @Transactional(readOnly = true)
    public List<PsychoAlertDTO> getPsychoRiskAlerts(Long coachId) {
        List<PsychoAlertRepository.AlertCardRow> rows = alertRepo.findOpenCardsByCoach(coachId);
        return rows.stream()
                .map(r -> new PsychoAlertDTO(
                        r.getAlert_id(),
                        r.getAthlete_id(),
                        r.getSession_id(),
                        r.getAnswer_id(),
                        r.getAthlete_name(),
                        r.getAthlete_photo(),
                        r.getSrpe(),
                        r.getFatigue(),
                        r.getSoreness(),
                        r.getMood(),
                        r.getEnergy(),
                        r.getTotal(),
                        r.getRisk(),
                        r.getDate()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PsychoAnswerAthleteDTO> getPsychoAnswerByAthlete(Long sessionId, Long athleteId) {
        Optional<PsychoAlertRepository.PsyAnswerRow> rows = alertRepo.findLatestOpenPsyAnswerBySessionAndAthlete(sessionId, athleteId);
        return rows.stream()
                .map(r -> new PsychoAnswerAthleteDTO(
                        r.getAnswerId(),
                        r.getAthleteId(),
                        r.getAthleteName(),
                        r.getAthleteEmail(),
                        r.getAthlete_position(),
                        r.getAthletePhoto(),
                        r.getSubmittedAt(),
                        r.getSrpe(),
                        r.getFatigue(),
                        r.getSoreness(),
                        r.getMood(),
                        r.getEnergy(),
                        r.getToken()
                ))
                .toList();
    }

    private int invert10(Integer v) {
        return Math.max(0, 10 - nz(v));
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }

    private RiskLevel mapLevel(int total) {
        if (total >= 30) return RiskLevel.HIGH;
        if (total >= 25) return RiskLevel.MODERATE;
        return RiskLevel.LOW;
    }
}
