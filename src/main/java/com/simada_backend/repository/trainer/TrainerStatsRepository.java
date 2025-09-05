package com.simada_backend.repository.trainer;

import com.simada_backend.model.session.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerStatsRepository extends JpaRepository<Sessao, Integer> {

    // Total de sessões do treinador (qualquer tipo)
    @Query(value = """
                SELECT COUNT(*) 
                FROM sessao s 
                WHERE s.id_treinador = :trainerId
            """, nativeQuery = true)
    long countTotalSessions(@Param("trainerId") int trainerId);

    // Treinos concluídos (tipo 'treino' e com término preenchido)
    @Query(value = """
                SELECT COUNT(*) 
                FROM sessao s
                WHERE s.id_treinador = :trainerId
                  AND s.tipo_sessao = 'Training'
                  AND s.data < CURDATE()
            """, nativeQuery = true)
    long countCompletedTrainings(@Param("trainerId") int trainerId);

    // Treinos desta semana (ISO week)
    @Query(value = """
                SELECT COUNT(*)
                FROM sessao s
                WHERE s.id_treinador = :trainerId
                  AND s.tipo_sessao = 'Training'
                  AND YEARWEEK(s.data, 1) = YEARWEEK(CURDATE(), 1)
            """, nativeQuery = true)
    long countTrainingsThisWeek(@Param("trainerId") int trainerId);

    // Jogos totais (tipo 'jogo')
    @Query(value = """
                SELECT COUNT(*)
                FROM sessao s
                WHERE s.id_treinador = :trainerId
                  AND s.tipo_sessao = 'Game'
                  AND s.data < CURDATE()
            """, nativeQuery = true)
    long countMatchesPlayed(@Param("trainerId") int trainerId);

    // Jogos no mês atual
    @Query(value = """
                SELECT COUNT(*)
                FROM sessao s
                WHERE s.id_treinador = :trainerId
                  AND s.tipo_sessao = 'Game'
                  AND YEAR(s.data) = YEAR(CURDATE())
                  AND MONTH(s.data) = MONTH(CURDATE())
            """, nativeQuery = true)
    long countMatchesThisMonth(@Param("trainerId") int trainerId);

    // Total de atletas do treinador (tabela Atleta)
    @Query(value = """
                SELECT COUNT(*)
                FROM atleta a
                WHERE a.id_treinador = :trainerId
            """, nativeQuery = true)
    long countTotalAthletes(@Param("trainerId") int trainerId);
}
