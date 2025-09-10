package com.simada_backend.repository.athlete;

import com.simada_backend.dto.response.athlete.home.PerfHighlight;
import com.simada_backend.model.athlete.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AthleteHomeRepository extends JpaRepository<Athlete, Long> {

    class Marker {
    }

    // === Athletes Coach ===
    @Query(value = """
            SELECT a.coach_id
            FROM athlete a
            WHERE a.id = :athleteId
            """, nativeQuery = true)
    Long findCoachIdByAthleteId(Long athleteId);

    // === Athletes List for Ranking ===
    @Query(value = """
            SELECT a.id
            FROM athlete a
            WHERE a.coach_id = :coachId
            """, nativeQuery = true)
    List<Long> listAthletesByCoach(Long coachId);

    @Query(value = """
            SELECT points, `position`
            FROM athlete_performance_snapshot
            WHERE athlete_id = :athleteId
            ORDER BY as_of DESC
            LIMIT 1
            """, nativeQuery = true)
    Object findLatestPerfRaw(Long athleteId);

    // === Recent Game ===
    @Query(value = """
            SELECT s.id, s.id_coach, s.date, s.session_type, s.title, NULL as opponent
            FROM `session` s
            JOIN metrics m ON m.id_session = s.id AND m.id_athlete = :athleteId
            WHERE UPPER(s.session_type) = 'GAME' AND s.date <= :today
            ORDER BY s.date DESC
            LIMIT 1
            """, nativeQuery = true)
    Object findRecentGameForAthleteRaw(Long athleteId, LocalDate today);

    // === Next Game ===
    @Query(value = """
            SELECT s.id, s.id_coach, s.date, s.session_type, s.title, NULL as opponent
            FROM `session` s
            JOIN metrics m ON m.id_session = s.id AND m.id_athlete = :athleteId
            WHERE UPPER(s.session_type) = 'GAME' AND s.date >= :today
            ORDER BY s.date ASC
            LIMIT 1
            """, nativeQuery = true)
    Object findNextGameForAthleteRaw(Long athleteId, LocalDate today);

    // === Calendar ===
    @Query(value = """
            SELECT s.date, s.session_type, s.title
            FROM `session` s
            JOIN metrics m ON m.id_session = s.id AND m.id_athlete = :athleteId
            WHERE s.date >= :fromDate AND s.date < :toDate
            ORDER BY s.date ASC
            """, nativeQuery = true)
    List<Object[]> findCalendarForAthleteRaw(Long athleteId, LocalDate fromDate, LocalDate toDate);

    record SessionRow(Long id, Long coachId, LocalDate date, String type, String title, String opponent) {}

    default SessionRow findRecentGameForAthlete(Long athleteId, LocalDate today) {
        Object row = findRecentGameForAthleteRaw(athleteId, today);
        if (row == null) return null;
        Object[] a = (Object[]) row;
        return new SessionRow(
                ((Number) a[0]).longValue(),
                ((Number) a[1]).longValue(),
                ((java.sql.Date) a[2]).toLocalDate(),
                (String) a[3],
                (String) a[4],
                (String) a[5] // sempre null aqui, mantido por compatibilidade
        );
    }

    default SessionRow findNextGameForAthlete(Long athleteId, LocalDate today) {
        Object row = findNextGameForAthleteRaw(athleteId, today);
        if (row == null) return null;
        Object[] a = (Object[]) row;
        return new SessionRow(
                ((Number) a[0]).longValue(),
                ((Number) a[1]).longValue(),
                ((java.sql.Date) a[2]).toLocalDate(),
                (String) a[3],
                (String) a[4],
                (String) a[5]
        );
    }

    record CalRow(LocalDate date, String type, String title) {
    }


    default List<CalRow> findCalendarForAthlete(Long athleteId, LocalDate fromDate, LocalDate toDate) {
        return findCalendarForAthleteRaw(athleteId, fromDate, toDate).stream()
                .map(a -> new CalRow(
                        ((java.sql.Date) a[0]).toLocalDate(),
                        (String) a[1],
                        (String) a[2]
                ))
                .toList();
    }

    default PerfHighlight findLatestPerf(Long athleteId) {
        Object row = findLatestPerfRaw(athleteId);
        if (row == null) return null;
        Object[] a = (Object[]) row;
        int points = ((Number) a[0]).intValue();
        int position = ((Number) a[1]).intValue();
        return new PerfHighlight(points, position);
    }
}
