package com.simada_backend.service;

import com.simada_backend.dto.response.AlertDTO;
import com.simada_backend.dto.response.athlete.AthleteDTO;
import com.simada_backend.dto.response.TopPerformerDTO;
import com.simada_backend.repository.session.TrainerSessionsRepository;
import com.simada_backend.repository.trainer.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TrainerService {
    private final RankingRepository repo;
    private final TrainerStatsRepository statsRepo;
    private final TrainerAlertsRepository alertsRepo;
    private final TrainerAthletesRepository trainerAthletesRepo;

    public TrainerService(
            RankingRepository repo,
            TrainerStatsRepository trainerStatsRepository,
            TrainerAlertsRepository alertsRepository,
            TrainerSessionsRepository sessionsRepo,
            TrainerAthletesRepository trainerAthletesRepo
    ) {
        this.repo = Objects.requireNonNull(repo);
        this.statsRepo = Objects.requireNonNull(trainerStatsRepository);
        this.alertsRepo = Objects.requireNonNull(alertsRepository);
        this.trainerAthletesRepo = Objects.requireNonNull(trainerAthletesRepo);
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

    public Map<String, Object> getTrainerStats(int trainerId) {
//        long totalSessions = statsRepo.countTotalSessions(trainerId);
//        long completedTrainings = statsRepo.countCompletedTrainings(trainerId);
//        long trainingsThisWeek = statsRepo.countTrainingsThisWeek(trainerId);
//        long matchesPlayed = statsRepo.countMatchesPlayed(trainerId);
//        long matchesThisMonth = statsRepo.countMatchesThisMonth(trainerId);
//        long totalAthletes = statsRepo.countTotalAthletes(trainerId);

//        return Map.of(
//                "completedTrainings", completedTrainings,
//                "trainingsThisWeek", trainingsThisWeek,
//                "matchesPlayed", matchesPlayed,
//                "matchesThisMonth", matchesThisMonth,
//                "totalSessions", totalSessions,
//                "totalAthletes", totalAthletes
//        );
        return Map.of(
                "completedTraining", 20,
                "trainingThisWeek", 2,
                "matchesPlayed", 10,
                "matchesThisMonth", 2,
                "totalSessions", 30,
                "totalAthletes", 25
        );
    }

    public List<AlertDTO> getTrainerAlerts(int trainerId, int days, int limit, String category) {
        int safeDays = Math.max(1, Math.min(days, 90));
        int safeLimit = Math.max(1, Math.min(limit, 100));
        String cat = (category == null || category.isBlank()) ? null : category;

//        return alertsRepo.findTrainerAlerts(trainerId, safeDays, safeLimit, cat)
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

        if ("PSICO".equals(cat)) {
            // MOCK PSICO
            return List.of(
                    new AlertDTO(
                            101L,
                            LocalDateTime.now().minusDays(1),
                            "PSICO",
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
                            "PSICO",
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

    public List<AthleteDTO> getAthletesTrainer(int trainerId, String q, int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int safeOffset = Math.max(0, offset);


        List<TrainerAthletesRepository.AthleteRow> rows =
                trainerAthletesRepo.findAthletes(trainerId, (q == null || q.isBlank()) ? null : q, safeLimit, safeOffset);

        return rows.stream()
                .map(r -> new AthleteDTO(
                        r.getId(),
                        r.getName(),
                        r.getEmail(),
                        r.getBirth() != null ? r.getBirth().toString() : null,
                        r.getPhone(),
                        r.getShirt_number() != null ? String.valueOf(r.getShirt_number()) : null,
                        r.getPosition(),
                        r.getAvatar_url()
                ))
                .toList();
    }


}