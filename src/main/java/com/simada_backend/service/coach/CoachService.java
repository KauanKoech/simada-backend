package com.simada_backend.service.coach;

import com.simada_backend.dto.response.athlete.AthleteDTO;
import com.simada_backend.dto.response.TopPerformerDTO;
import com.simada_backend.dto.response.athlete.AthleteExtraDTO;
import com.simada_backend.repository.athlete.AthleteExtraRepository;
import com.simada_backend.repository.coach.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CoachService {
    private final CoachStatsRepository statsRepo;
    private final CoachAthletesRepository coachAthletesRepo;
    private final AthleteExtraRepository athleteExtraRepo;
    private final RankingRepository rankingRepository;

    public List<TopPerformerDTO> getTopPerformers(int limit) {
        int safe = Math.max(1, Math.min(limit, 50));
        return rankingRepository.findTopPerformers(safe).stream()
                .map(r -> new TopPerformerDTO(
                        r.getAthlete_name(),
                        r.getFoto(),
                        r.getDate(),
                        r.getScore(),
                        r.getLast_score()
                ))
                .toList();
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