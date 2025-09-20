package com.simada_backend.service.psycho;

import com.simada_backend.api.error.BusinessException;
import com.simada_backend.api.error.ErrorCode;
import com.simada_backend.dto.request.psycho.PsychoFormSubmitRequest;
import com.simada_backend.dto.response.psycho.PsychoAnswerDTO;
import com.simada_backend.model.Coach;
import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.psycho.PsychoFormAnswer;
import com.simada_backend.model.psycho.PsychoFormInvite;
import com.simada_backend.model.session.Session;
import com.simada_backend.repository.athlete.AthleteRepository;
import com.simada_backend.repository.coach.CoachRepository;
import com.simada_backend.repository.psycho.PsychoFormInviteRepository;
import com.simada_backend.repository.psycho.PsychoFormAnswerRepository;
import com.simada_backend.repository.session.MetricsRepository;
import com.simada_backend.repository.session.SessionRepository;
import com.simada_backend.service.alert.PsychoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PsychoFormService {

    private final PsychoService psychoService;
    private final PsychoFormInviteRepository inviteRepo;
    private final PsychoFormAnswerRepository answerRepo;
    private final CoachRepository coachRepo;
    private final AthleteRepository athleteRepo;
    private final SessionRepository sessionRepo;
    private final MetricsRepository metricsRepo;
    private final JavaMailSender mailSender;

    @Transactional
    public List<PsychoFormInvite> createConvites(Long coachId, Long sessionId, String publicBaseUrl) {
        if (coachId == null || sessionId == null) {
            throw new IllegalArgumentException("coachId and sessionId can't be null.");
        }

        // atletas da sessão
        List<Long> athleteIds = metricsRepo.findAthletesBySessionId(sessionId);
        if (athleteIds == null || athleteIds.isEmpty()) {
            throw new RuntimeException("No athletes found; add them to the system first.");
        }

        // ENTIDADES (falha rápido se não encontrar)
        Coach coach = coachRepo.findById(coachId)
                .orElseThrow(() -> new IllegalArgumentException("Coach not found."));
        Session session = sessionRepo.findById(sessionId.intValue())
                .orElseThrow(() -> new IllegalArgumentException("Session not found."));

        List<PsychoFormInvite> convites = new ArrayList<>();

        for (Long athleteId : athleteIds) {
            String email = athleteRepo.findEmailByAthleteId(athleteId)
                    .orElseThrow(() -> new RuntimeException("E-mail not found for athleteId: " + athleteId));

            Athlete athlete = athleteRepo.findById(athleteId)
                    .orElseThrow(() -> new IllegalArgumentException("Athlete not found: " + athleteId));

            PsychoFormInvite convite = new PsychoFormInvite();
            convite.setToken(UUID.randomUUID().toString());
            convite.setIdCoach(coach);
            convite.setIdAthlete(athlete);
            convite.setIdSession(session);
            convite.setEmail(email);
            convite.setStatus("PENDING");
            convite.setCreatedAt(LocalDateTime.now());
            convite.setExpiresAt(LocalDateTime.now().plusDays(2));

            inviteRepo.save(convite);
            convites.add(convite);

            String base = (publicBaseUrl != null && !publicBaseUrl.isBlank()) ? publicBaseUrl : "http://localhost:3000";
            String url = base + "/psycho-form/" + convite.getToken();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Psycho-Emotional Questionnaire — WIKO");
            message.setText("""
                    Hello!
                    
                    You’ve been invited to answer the psycho-emotional form for session #%s.
                    Open the link below to answer:
                    
                    %s
                    
                    This link expires in 48 hours.
                    
                    — WIKO
                    """.formatted(sessionId, url));
            mailSender.send(message);
        }

        return convites;
    }

    public PsychoFormInvite validateToken(String token) {
        PsychoFormInvite convite = inviteRepo.findByToken(token)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TOKEN_INVALID,
                        HttpStatus.UNAUTHORIZED,
                        "Invalid token."
                ));

        if (!"PENDING".equalsIgnoreCase(convite.getStatus())) {
            throw new RuntimeException("This form was answered or expired.");
        }
        if (convite.getExpiresAt() == null || convite.getExpiresAt().isBefore(LocalDateTime.now())) {
            convite.setStatus("EXPIRED");
            inviteRepo.save(convite);
            throw new BusinessException(
                    ErrorCode.TOKEN_EXPIRED,
                    HttpStatus.UNAUTHORIZED,
                    "Expired token."
            );
        }
        return convite;
    }

    @Transactional
    public void submitForm(String token, PsychoFormSubmitRequest req) {
        PsychoFormInvite convite = inviteRepo.findByToken(token)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TOKEN_INVALID,
                        HttpStatus.UNAUTHORIZED,
                        "Invalid token."
                ));

        if (!"PENDING".equalsIgnoreCase(convite.getStatus())) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    HttpStatus.BAD_REQUEST,
                    "This form has already been answered or is not pending."
            );
        }

        if (convite.getExpiresAt() == null || convite.getExpiresAt().isBefore(LocalDateTime.now())) {
            convite.setStatus("EXPIRED");
            inviteRepo.save(convite);
            throw new BusinessException(
                    ErrorCode.TOKEN_EXPIRED,
                    HttpStatus.UNAUTHORIZED,
                    "Expired token."
            );
        }

        Athlete athlete = athleteRepo.findById(convite.getIdAthlete().getId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Athlete not found."
                ));

        Session session = sessionRepo.findById(convite.getIdSession().getId().intValue())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Session not found."
                ));


        PsychoFormAnswer resp = new PsychoFormAnswer();
        resp.setToken(token);
        resp.setAthlete(athlete);
        resp.setIdSession(session);
        resp.setSrpe(req.getSrpe());
        resp.setFatigue(req.getFatigue());
        resp.setSoreness(req.getSoreness());
        resp.setMood(req.getMood());
        resp.setEnergy(req.getEnergy());
        resp.setSubmittedAt(LocalDateTime.now());

        answerRepo.save(resp);

        convite.setStatus("ANSWERED");
        inviteRepo.save(convite);

        psychoService.processAnswer(convite, resp);
    }

    public List<PsychoAnswerDTO> getPsychoAnswersBySession(Long sessionId) {
        if (sessionId == null) throw new IllegalArgumentException("sessionId cannot be null.");

        List<PsychoFormAnswerRepository.PsychoAnswerRow> rows = answerRepo.findAnswersBySession(sessionId);

        return rows.stream()
                .map(r -> new PsychoAnswerDTO(
                        r.getId(),
                        r.getId_session(),
                        r.getId_athlete(),
                        r.getToken(),
                        r.getSubmitted_at(),
                        r.getSRPE(),
                        r.getFatigue(),
                        r.getSoreness(),
                        r.getMood(),
                        r.getEnergy(),
                        r.getAthlete_name(),
                        r.getAthlete_email(),
                        r.getAthlete_photo(),
                        r.getAthlete_position()
                ))
                .toList();
    }

}
