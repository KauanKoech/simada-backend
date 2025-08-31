package com.simada_backend.service.session;

import com.simada_backend.dto.request.RegisterSessionRequest;
import com.simada_backend.dto.response.TrainerSessionDTO;
import com.simada_backend.model.Sessao;
import com.simada_backend.model.Treinador;
import com.simada_backend.repository.session.MetricasRepository;
import com.simada_backend.repository.session.TrainerSessionsRepository;
import com.simada_backend.repository.trainer.TrainerRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class SessionService {

    private final TrainerSessionsRepository sessionsRepo;
    private final TrainerRepository trainerRepo;
    private final MetricasRepository metricasRepo;

    public SessionService(TrainerSessionsRepository sessionsRepo, TrainerRepository trainerRepo, MetricasRepository metricasRepo) {
        this.sessionsRepo = Objects.requireNonNull(sessionsRepo);
        this.trainerRepo = Objects.requireNonNull(trainerRepo);
        this.metricasRepo = Objects.requireNonNull(metricasRepo);
    }

    public List<TrainerSessionDTO> getSessionsTrainer(
            int trainerId,
            LocalDate from,
            LocalDate to,
            int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return sessionsRepo.findSessions(trainerId, from, to, safeLimit)
                .stream()
                .map(r -> new TrainerSessionDTO(
                        r.getId(),
                        r.getTrainer_id(),
                        r.getTrainer_photo(),
                        r.getDate(),
                        r.getType(),
                        r.getTitle(),
                        r.getAthletes_count(),
                        r.getScore(),
                        r.getDescription(),
                        r.getLocation(),
                        r.getHas_metrics() != null && r.getHas_metrics() > 0
                ))
                .toList();


    }

    @Transactional
    public void registerSession(RegisterSessionRequest req) {
        Treinador treinador = trainerRepo.findById(req.getTrainerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Treinador não encontrado"));

        LocalDate date;
        try {
            date = LocalDate.parse(req.getDate());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato inválido para 'date' (use YYYY-MM-DD)");
        }

        String trainerPhoto = (treinador.getUsuario() != null) ? treinador.getUsuario().getFoto() : null;

        Sessao s = new Sessao();
        s.setTreinador(treinador);
        s.setFotoTreinador(trainerPhoto);
        s.setData(date);
        s.setTipoSessao(req.getType());
        s.setTitulo(req.getTitle());
        s.setDescricao(req.getNotes());
        s.setLocal(req.getLocation());
        s.setPlacar(req.getScore());
        s.setNumAtletas(req.getAthletesCount());

        sessionsRepo.save(s);
    }

    @Transactional
    public void deleteSession(int sessionId) {
        Sessao s = sessionsRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sessão não encontrada"));

        metricasRepo.deleteBySessionId(sessionId);
        sessionsRepo.delete(s);
    }
}
