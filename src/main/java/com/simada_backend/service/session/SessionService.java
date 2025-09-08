package com.simada_backend.service.session;

import com.simada_backend.dto.request.session.RegisterSessionRequest;
import com.simada_backend.dto.response.coach.CoachSessionDTO;
import com.simada_backend.model.session.Session;
import com.simada_backend.model.Coach;
import com.simada_backend.repository.session.MetricsRepository;
import com.simada_backend.repository.session.CoachSessionsRepository;
import com.simada_backend.repository.coach.CoachRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class SessionService {

    private final CoachSessionsRepository sessionsRepo;
    private final CoachRepository coachRepo;
    private final MetricsRepository metricasRepo;

    public SessionService(CoachSessionsRepository sessionsRepo, CoachRepository coachRepo, MetricsRepository metricasRepo) {
        this.sessionsRepo = Objects.requireNonNull(sessionsRepo);
        this.coachRepo = Objects.requireNonNull(coachRepo);
        this.metricasRepo = Objects.requireNonNull(metricasRepo);
    }

    public List<CoachSessionDTO> getSessionsCoach(
            int coachId,
            LocalDate from,
            LocalDate to,
            int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return sessionsRepo.findSessions(coachId, from, to, safeLimit)
                .stream()
                .map(r -> new CoachSessionDTO(
                        r.getId(),
                        r.getCoach_id(),
                        r.getCoach_photo(),
                        r.getDate(),
                        r.getType(),
                        r.getTitle(),
                        r.getAthletes_count(),
                        r.getScore(),
                        r.getDescription(),
                        r.getLocal(),
                        r.getHas_metrics() != null && r.getHas_metrics() > 0,
                        r.getHas_psycho() != null && r.getHas_psycho() > 0
                ))
                .toList();


    }

    @Transactional
    public void registerSession(RegisterSessionRequest req) {
        Coach coach = coachRepo.findById(req.getCoachId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Treinador não encontrado"));

        LocalDate date;
        try {
            date = LocalDate.parse(req.getDate());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato inválido para 'date' (use YYYY-MM-DD)");
        }

        String coachPhoto = (coach.getUser() != null) ? coach.getUser().getPhoto() : null;

        Session s = new Session();
        s.setCoach(coach);
        s.setCoach_Photo(coachPhoto);
        s.setDate(date);
        s.setSession_type(req.getType());
        s.setTitle(req.getTitle());
        s.setDescription(req.getNotes());
        s.setLocal(req.getLocation());
        s.setScore(req.getScore());
        s.setNumAthletes(req.getAthletesCount());

        sessionsRepo.save(s);
    }

    @Transactional
    public void deleteSession(Integer sessionId) {
        Session s = sessionsRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sessão não encontrada"));

        metricasRepo.deleteBySessionId(sessionId);
        sessionsRepo.delete(s);
    }
}
