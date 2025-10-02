package com.simada_backend.repository.psycho;

import com.simada_backend.model.psycho.PsychoFormInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PsychoFormInviteRepository extends JpaRepository<PsychoFormInvite, Long> {
    Optional<PsychoFormInvite> findByToken(String token);

    @Modifying
    @Query("delete from PsychoFormInvite i where i.idAthlete.id = :athleteId")
    void deleteByAthleteId(@Param("athleteId") Long athleteId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PsychoFormInvite pfi where pfi.idSession.id = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);
}