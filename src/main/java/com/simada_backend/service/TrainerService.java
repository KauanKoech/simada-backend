package com.simada_backend.service;

import com.simada_backend.dto.response.AlertDTO;
import com.simada_backend.dto.response.AthleteDTO;
import com.simada_backend.dto.response.TopPerformerDTO;
import com.simada_backend.dto.response.TrainerSessionDTO;
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
    private final TrainerSessionsRepository sessionsRepo;
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
        this.sessionsRepo = Objects.requireNonNull(sessionsRepo);
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

    public List<TrainerSessionDTO> getSessionsTrainer(int trainerId, LocalDateTime from,
                                                      LocalDateTime to, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
//        return sessionsRepo.findSessions(trainerId, from, to, safeLimit)
//                .stream()
//                .map(r -> new TrainerSessionDTO(
//                        r.getId(),
//                        r.getTrainer_id(),
//                        r.getTrainer_photo(),
//                        r.getStart(),
//                        r.getEnd(),
//                        r.getType(),
//                        r.getTitle(),
//                        r.getAthleteCount(),
//                        r.getScore(),
//                        r.getDescription(),
//                        r.getLocation()
//                ))
//                .toList();

        String photo = "https://i.pravatar.cc/150?img=10";
        LocalDateTime now = LocalDateTime.now();

        return List.of(
                new TrainerSessionDTO(
                        101L,
                        (long) trainerId,
                        photo,
                        now.minusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0),
                        now.minusDays(1).withHour(19).withMinute(30).withSecond(0).withNano(0),
                        "training",
                        "Treino — Força",
                        20,
                        null, // score (depois ligamos ao banco)
                        "Trabalho de força e core",
                        "Quadra A"
                ),
                new TrainerSessionDTO(
                        102L,
                        (long) trainerId,
                        photo,
                        now.minusDays(2).withHour(17).withMinute(0).withSecond(0).withNano(0),
                        now.minusDays(2).withHour(18).withMinute(15).withSecond(0).withNano(0),
                        "training",
                        "Treino — Velocidade",
                        22,
                        null,
                        "Sprints e pliometria",
                        "Campo 1"
                ),
                new TrainerSessionDTO(
                        103L,
                        (long) trainerId,
                        photo,
                        now.minusDays(3).withHour(19).withMinute(0).withSecond(0).withNano(0),
                        now.minusDays(3).withHour(20).withMinute(30).withSecond(0).withNano(0),
                        "game",
                        "Jogo — Amistoso vs. Tigres",
                        14,
                        "2–1", // exemplo de score preenchido em jogos
                        "Amistoso de preparação",
                        "Estádio Municipal"
                ),
                new TrainerSessionDTO(
                        104L,
                        (long) trainerId,
                        photo,
                        now.plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0),
                        now.plusDays(1).withHour(19).withMinute(0).withSecond(0).withNano(0),
                        "training",
                        "Treino — Tático",
                        15,
                        null,
                        "Saída de bola e pressão alta",
                        "Quadra B"
                ),
                new TrainerSessionDTO(
                        105L,
                        (long) trainerId,
                        photo,
                        now.plusDays(3).withHour(16).withMinute(30).withSecond(0).withNano(0),
                        now.plusDays(3).withHour(18).withMinute(0).withSecond(0).withNano(0),
                        "game",
                        "Jogo — Campeonato Regional",
                        13,
                        "3-0",
                        "Rodada 8",
                        "Arena Central"
                )
        );
    }

    public List<AthleteDTO> getAthletesTrainer(int trainerId, String q, String status, int limit, int offset) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        int safeOffset = Math.max(0, offset);

//        var rows = trainerAthletesRepo.findAthletes(trainerId,
//                (q == null || q.isBlank()) ? null : q,
//                (status == null || status.isBlank()) ? null : status,
//                safeLimit, safeOffset);

//        return rows.stream()
//                .map(r -> new AthleteDTO(
//                        r.getId(),
//                        r.getName(),
//                        r.getEmail(),
//                        r.getBirth(),
//                        r.getPhone(),
//                        r.getAvatar_url(),
//                        r.getStatus()
//                ))
//                .toList();

        return List.of(
                new AthleteDTO(1L, "João Silva", "joao@ex.com", java.time.LocalDate.of(2001, 3, 15), "1199999-0001", "https://i.pravatar.cc/150?img=12", "active"),
                new AthleteDTO(2L, "Maria Souza", "maria@ex.com", java.time.LocalDate.of(1999, 8, 30), "2198888-0002", "https://i.pravatar.cc/150?img=13", "injured"),
                new AthleteDTO(3L, "Carlos Lima", "carlos@ex.com", java.time.LocalDate.of(2002, 1, 10), null, "https://i.pravatar.cc/150?img=14", "inactive")
        );
    }
}