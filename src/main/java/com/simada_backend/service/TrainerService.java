package com.simada_backend.service;

import com.simada_backend.dto.response.TopPerformerDTO;
import com.simada_backend.repository.RankingRepository;
import com.simada_backend.repository.TrainerStatsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TrainerService {
    private final RankingRepository repo;
    private final TrainerStatsRepository statsRepo;

    public TrainerService(RankingRepository repo, TrainerStatsRepository trainerStatsRepository) {
        this.repo = Objects.requireNonNull(repo);
        this.statsRepo = Objects.requireNonNull(trainerStatsRepository);
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
}