package com.simada_backend.repository.coach;

import com.simada_backend.model.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoachStatsRepository extends JpaRepository<Session, Integer> {

    // Total de sessões do treinador (qualquer tipo)
    @Query(value = """
                SELECT COUNT(*) 
                FROM session s 
                WHERE s.id_coach = :coachId
            """, nativeQuery = true)
    long countTotalSessions(@Param("coachId") int coachId);

    // Treinos concluídos (tipo 'treino' e com término preenchido)
    @Query(value = """
                SELECT COUNT(*) 
                FROM session s
                WHERE s.id_coach = :coachId
                  AND s.session_type = 'Training'
                  AND s.date < CURDATE()
            """, nativeQuery = true)
    long countCompletedTrainings(@Param("coachId") int coachId);

    // Treinos desta semana (ISO week)
    @Query(value = """
                SELECT COUNT(*)
                FROM session s
                WHERE s.id_coach = :coachId
                  AND s.session_type = 'Training'
                  AND YEARWEEK(s.date, 1) = YEARWEEK(CURDATE(), 1)
            """, nativeQuery = true)
    long countTrainingsThisWeek(@Param("coachId") int coachId);

    // Jogos totais (tipo 'jogo')
    @Query(value = """
                SELECT COUNT(*)
                FROM session s
                WHERE s.id_coach = :coachId
                  AND s.session_type = 'Game'
                  AND s.date < CURDATE()
            """, nativeQuery = true)
    long countMatchesPlayed(@Param("coachId") int coachId);

    // Jogos no mês atual
    @Query(value = """
                SELECT COUNT(*)
                FROM session s
                WHERE s.id_coach = :coachId
                  AND s.session_type = 'Game'
                  AND YEAR(s.date) = YEAR(CURDATE())
                  AND MONTH(s.date) = MONTH(CURDATE())
            """, nativeQuery = true)
    long countMatchesThisMonth(@Param("coachId") int coachId);

    // Total de atletas do treinador (tabela Atleta)
    @Query(value = """
                SELECT COUNT(*)
                FROM athlete a
                WHERE a.id_coach = :coachId
            """, nativeQuery = true)
    long countTotalAthletes(@Param("coachId") int coachId);
}
