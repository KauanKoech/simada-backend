package com.simada_backend.service;

import com.simada_backend.dto.response.PsychoAlertDTO;
import com.simada_backend.dto.response.PsychoAnswerAthleteDTO;
import com.simada_backend.model.psychoForm.*;
import com.simada_backend.repository.psycho.PsychoAlertRepository;
import com.simada_backend.repository.psycho.PsychoRiskScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AlertsService {

    private final PsychoRiskScoreRepository riskRepo;
    private final PsychoAlertRepository alertRepo;

    public AlertsService(PsychoRiskScoreRepository riskRepo, PsychoAlertRepository alertRepo) {
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

        // 1) salva o score
        PsychoRiskScore prs = new PsychoRiskScore();
        prs.setCoachId(invite.getIdCoach());
        prs.setAthleteId(invite.getIdAthlete());
        prs.setSessionId(invite.getIdSession());
        prs.setAnswerId(answer.getId());
        prs.setRiskLevel(level);
        prs.setTotalScore(total);
        prs.setCreatedAt(LocalDateTime.now());
        riskRepo.save(prs);

        // 2) cria alerta somente se MODERATE/HIGH
        if (level == RiskLevel.MODERATE || level == RiskLevel.HIGH) {
            PsychoAlert alert = new PsychoAlert();
            alert.setCoachId(invite.getIdCoach());
            alert.setAthleteId(invite.getIdAthlete());
            alert.setSessionId(invite.getIdSession());
            alert.setAnswerId(answer.getId());
            alert.setRuleCode(level == RiskLevel.HIGH ? "PSYCHO_HIGH" : "PSYCHO_MODERATE");
            alert.setSeverity(level == RiskLevel.HIGH ? "CRITICAL" : "WARN");

            String msg = "Score: " + total + " (" + (level == RiskLevel.HIGH ? "Risco Elevado" : "Atenção") + "). "
                    + "Detalhe — sRPE: " + srpe
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
