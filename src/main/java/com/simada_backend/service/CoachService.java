package com.simada_backend.service;

import com.simada_backend.dto.response.AlertDTO;
import com.simada_backend.dto.response.athlete.AthleteDTO;
import com.simada_backend.dto.response.TopPerformerDTO;
import com.simada_backend.dto.response.athlete.AthleteExtraDTO;
import com.simada_backend.repository.athlete.AthleteExtraRepository;
import com.simada_backend.repository.coach.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CoachService {
    private final CoachStatsRepository statsRepo;
    private final CoachAthletesRepository coachAthletesRepo;
    private final AthleteExtraRepository athleteExtraRepo;

    public CoachService(
            CoachStatsRepository coachStatsRepository,
            CoachAthletesRepository coachAthletesRepo,
            AthleteExtraRepository athleteExtraRepository
    ) {
        this.statsRepo = Objects.requireNonNull(coachStatsRepository);
        this.coachAthletesRepo = Objects.requireNonNull(coachAthletesRepo);
        this.athleteExtraRepo = Objects.requireNonNull(athleteExtraRepository);
    }

    public List<TopPerformerDTO> getTopPerformers(int limit) {
        int safe = Math.max(1, Math.min(limit, 50));
//        return repo.findTopPerformers(safe).stream()
//                .map(r -> new TopPerformerDTO(
//                        r.getNome_atleta(),
//                        r.getFoto(),
//                        r.getData_atualizacao(),
//                        r.getPontuacao(),
//                        r.getUltima_pontuacao()
//                ))
//                .toList();
        return List.of(
                new TopPerformerDTO("João Silva", null, LocalDateTime.now(), 95.0, 90.0),
                new TopPerformerDTO("Maria Souza", null, LocalDateTime.now(), 88.0, 85.0)
        );
    }

    public Map<String, Object> getCoachStats(int coachId) {
        long totalSessions = statsRepo.countTotalSessions(coachId);
        long completedTrainings = statsRepo.countCompletedTrainings(coachId);
        long trainingsThisWeek = statsRepo.countTrainingsThisWeek(coachId);
        long matchesPlayed = statsRepo.countMatchesPlayed(coachId);
        long matchesThisMonth = statsRepo.countMatchesThisMonth(coachId);
        long totalAthletes = statsRepo.countTotalAthletes(coachId);

        return Map.of(
                "completedTrainings", completedTrainings,
                "trainingsThisWeek", trainingsThisWeek,
                "matchesPlayed", matchesPlayed,
                "matchesThisMonth", matchesThisMonth,
                "totalSessions", totalSessions,
                "totalAthletes", totalAthletes
        );
    }

    public List<AlertDTO> getCoachAlerts(int coachId, int days, int limit, String category) {
        int safeDays = Math.max(1, Math.min(days, 90));
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String cat = (category == null || category.isBlank()) ? null : category;

//        return alertsRepo.findCoachAlerts(coachId, safeDays, safeLimit, cat)
//                .stream()
//                .map(r -> new AlertDTO(
//                        r.getId(),
//                        r.getDate(),
//                        r.getType(),
//                        r.getMessage(),
//                        r.getStatus(),
//                        r.getAction(),
//                        r.getAthlete_name(),
//                        r.getAthlete_photo(),
//                        r.getPrev_value(),
//                        r.getCurr_value(),
//                        r.getPercent(),
//                        r.getUnit()
//                ))
//                .toList();

        if ("PSYCHO".equals(cat)) {
            // MOCK PSYCHO
            return List.of(
                    new AlertDTO(
                            101L,
                            LocalDateTime.now().minusDays(1),
                            "PSYCHO",
                            "Fadiga elevada detectada no questionário diário",
                            "CAUTION",
                            "Rever carga desta semana",
                            "João Silva",
                            "https://i.pravatar.cc/150?img=3",
                            null, null, null, null,
                            "Alto", "Neutro", 5
                    ),
                    new AlertDTO(
                            102L,
                            LocalDateTime.now().minusDays(2),
                            "PSYCHO",
                            "Qualidade de sono abaixo do ideal",
                            "LOW",
                            "Orientar higiene do sono",
                            "Maria Souza",
                            "https://i.pravatar.cc/150?img=4",
                            null, null, null, null,
                            "Moderada", "Ruim", 4
                    )
            );
        } else {
            return List.of(
                    new AlertDTO(
                            1L,
                            LocalDateTime.now().minusDays(1),
                            "PERFORMANCE",
                            "Alerta: queda de performance detectada",
                            "ABERTO",
                            "Recomendar descanso",
                            "João Silva",
                            "https://i.pravatar.cc/150?img=1",
                            90.0, 75.0, -16.6, "PlayerLoad",   // performance
                            null, null, null                   // psico
                    ),
                    new AlertDTO(
                            2L,
                            LocalDateTime.now().minusDays(2),
                            "PERFORMANCE",
                            "Alerta: alta carga aguda detectada",
                            "RESOLVIDO",
                            "Reduzir intensidade",
                            "Maria Souza",
                            "https://i.pravatar.cc/150?img=2",
                            60.0, 95.0, 58.3, "ACWR",
                            null, null, null
                    )
            );
        }
    }

    public List<AthleteDTO> getAthletesCoach(int coachId, String q, int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int safeOffset = Math.max(0, offset);

        List<CoachAthletesRepository.AthleteRow> rows =
                coachAthletesRepo.findAthletes(coachId, (q == null || q.isBlank()) ? null : q, safeLimit, safeOffset);

        return rows.stream().map(r -> {
            // Buscar extras do atleta
            var extraOpt = athleteExtraRepo.findByAthlete_Id(r.getId());

            AthleteExtraDTO extra = extraOpt.map(e -> new AthleteExtraDTO(
                    e.getHeightCm(),
                    e.getWeightKg(),
                    e.getLeanMassKg(),
                    e.getFatMassKg(),
                    e.getBodyFatPct(),
                    e.getDominantFoot(),
                    e.getNationality(),
                    e.getInjuryStatus()
            )).orElse(null);

            return new AthleteDTO(
                    r.getId(),
                    r.getName(),
                    r.getEmail(),
                    r.getBirth() != null ? r.getBirth().toString() : null,
                    r.getPhone(),
                    r.getJersey_number() != null ? String.valueOf(r.getJersey_number()) : null,
                    r.getPosition(),
                    r.getPhoto(),
                    extra
            );
        }).toList();
    }

}