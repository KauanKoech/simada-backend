package com.simada_backend.service;

import com.simada_backend.dto.request.RegisterSessionRequest;
import com.simada_backend.dto.response.SessionDTO;
import com.simada_backend.dto.response.TrainerSessionDTO;
import com.simada_backend.model.Sessao;
import com.simada_backend.model.Treinador;
import com.simada_backend.repository.session.TrainerSessionsRepository;
import com.simada_backend.repository.trainer.TrainerRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@Service
public class SessionService {

    private final TrainerSessionsRepository sessionsRepo;
    private final TrainerRepository trainerRepo;

    public SessionService(TrainerSessionsRepository sessionsRepo, TrainerRepository trainerRepo) {
        this.sessionsRepo = Objects.requireNonNull(sessionsRepo);
        this.trainerRepo = trainerRepo;
    }

    public List<TrainerSessionDTO> getSessionsTrainer(
            int trainerId,
            LocalDateTime from,
            LocalDateTime to,
            int limit
    ) {
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

    @Transactional
    public SessionDTO registerSession(RegisterSessionRequest req) {
        // 1) Validações básicas
        if (req.getTrainerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "trainerId é obrigatório");
        }
        if (req.getType() == null || req.getType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type é obrigatório");
        }
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title é obrigatório");
        }
        if (req.getDate() == null || req.getDate().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date é obrigatório (formato YYYY-MM-DD)");
        }
        if (req.getAthletesCount() == null || req.getAthletesCount() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "athletesCount inválido");
        }

        Treinador treinador = trainerRepo.findById(req.getTrainerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Treinador não encontrado"));

        String tipoDb = mapTypeAppToDb(req.getType());

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
        s.setTipoSessao(tipoDb);
        s.setTitulo(req.getTitle());
        s.setDescricao(req.getNotes());
        s.setLocal(req.getLocation());
        s.setPlacar(req.getScore());
        s.setNumAtletas(req.getAthletesCount());

        Sessao saved = sessionsRepo.save(s);

        return new SessionDTO(
                saved.getIdSessao().longValue(),
                treinador.getId(),
                saved.getFotoTreinador(),
                saved.getData(),
                mapTypeDbToApp(saved.getTipoSessao()),
                saved.getTitulo(),
                saved.getNumAtletas(),
                saved.getPlacar(),
                saved.getDescricao(),
                saved.getLocal()
        );
    }

    private String mapTypeAppToDb(String type) {
        return switch (type.toLowerCase()) {
            case "training" -> "treino";
            case "game"     -> "jogo";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de sessão inválido: " + type);
        };
    }

    private String mapTypeDbToApp(String tipoDb) {
        if (tipoDb == null) return "training";
        return switch (tipoDb.toLowerCase()) {
            case "treino" -> "training";
            case "jogo", "game" -> "game";
            default -> "training";
        };
    }

}
