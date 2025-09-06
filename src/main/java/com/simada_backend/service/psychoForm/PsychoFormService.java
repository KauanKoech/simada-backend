package com.simada_backend.service.psychoForm;

import com.simada_backend.dto.request.psychoForm.PsychoFormSubmitRequest;
import com.simada_backend.dto.response.PsychoAnswerDTO;
import com.simada_backend.model.psychoForm.PsychoFormAnswer;
import com.simada_backend.model.psychoForm.PsychoFormInvite;
import com.simada_backend.repository.athlete.AthleteRepository;
import com.simada_backend.repository.psychoForm.PsychoFormInviteRepository;
import com.simada_backend.repository.psychoForm.PsychoFormAnswerRepository;
import com.simada_backend.repository.session.MetricsRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PsychoFormService {

    private final PsychoFormInviteRepository inviteRepo;
    private final PsychoFormAnswerRepository answerRepo;
    private final AthleteRepository athleteRepo;
    private final MetricsRepository metricsRepo;
    private final JavaMailSender mailSender;

    public PsychoFormService(PsychoFormInviteRepository inviteRepo,
                             PsychoFormAnswerRepository answerRepo,
                             AthleteRepository athleteRepo,
                             MetricsRepository metricsRepo,
                             JavaMailSender mailSender
    ) {
        this.inviteRepo = Objects.requireNonNull(inviteRepo);
        this.answerRepo = Objects.requireNonNull(answerRepo);
        this.athleteRepo = Objects.requireNonNull(athleteRepo);
        this.metricsRepo = Objects.requireNonNull(metricsRepo);
        this.mailSender = Objects.requireNonNull(mailSender);
    }

    public List<PsychoFormInvite> createConvites(Long coachId, Long sessionId, String publicBaseUrl) {
        if (coachId == null || sessionId == null) {
            throw new IllegalArgumentException("coachId e sessionId são obrigatórios.");
        }

        List<Long> athleteIds = metricsRepo.findAthletesBySessionId(sessionId);
        if (athleteIds == null || athleteIds.isEmpty()) {
            throw new RuntimeException("Any athlete was found, you need to add them in the system.");
        }

        List<PsychoFormInvite> convites = new ArrayList<>();

        for (Long athleteId : athleteIds) {
            String email = athleteRepo.findEmailByAthleteId(athleteId)
                    .orElseThrow(() -> new RuntimeException("E-mail not found for athleteId: " + athleteId));

            PsychoFormInvite convite = new PsychoFormInvite();
            convite.setToken(UUID.randomUUID().toString());
            convite.setIdCoach(coachId);
            convite.setIdAthlete(athleteId);
            convite.setIdSession(sessionId);
            convite.setEmail(email);
            convite.setStatus("PENDING");
            convite.setCreatedAt(LocalDateTime.now());
            convite.setExpiresAt(LocalDateTime.now().plusDays(2));

            inviteRepo.save(convite);
            convites.add(convite);

            String url = (publicBaseUrl != null && !publicBaseUrl.isBlank()
                    ? publicBaseUrl
                    : "http://localhost:3000") + "/psycho-form/" + convite.getToken();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Psycho-Emotional Questionnary - WIKO");
            message.setText(
                    "Hello!\n\n" +
                            "You have been invited to answer the psycho-emotional form for the session #" + sessionId + ".\n" +
                            "Access the link below to answer:\n\n" +
                            url + "\n\n" +
                            "This link expire in 48 hours.\n\n" +
                            "— WIKO"
            );
            mailSender.send(message);
        }

        return convites;
    }

    public PsychoFormInvite validateToken(String token) {
        PsychoFormInvite convite = inviteRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido."));

        if (!"PENDING".equalsIgnoreCase(convite.getStatus())) {
            throw new RuntimeException("Este formulário já foi respondido ou expirou.");
        }
        if (convite.getExpiresAt() == null || convite.getExpiresAt().isBefore(LocalDateTime.now())) {
            convite.setStatus("EXPIRED");
            inviteRepo.save(convite);
            throw new RuntimeException("Token expirado.");
        }
        return convite;
    }

    public void submitForm(String token, PsychoFormSubmitRequest req) {
        System.out.println("SRPE: " + req.getSrpe());
        PsychoFormInvite convite = inviteRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido."));

        if (!"PENDING".equalsIgnoreCase(convite.getStatus())) {
            throw new RuntimeException("Este formulário já foi respondido ou expirou.");
        }
        if (convite.getExpiresAt() == null || convite.getExpiresAt().isBefore(LocalDateTime.now())) {
            convite.setStatus("EXPIRED");
            inviteRepo.save(convite);
            throw new RuntimeException("Token expirado.");
        }

        PsychoFormAnswer resp = new PsychoFormAnswer();
        resp.setToken(token);
        resp.setIdAthlete(convite.getIdAthlete());
        resp.setIdSession(convite.getIdSession());
        resp.setSrpe(req.getSrpe());
        resp.setFatigue(req.getFatigue());
        resp.setSoreness(req.getSoreness());
        resp.setMood(req.getMood());
        resp.setEnergy(req.getEnergy());
        resp.setSubmittedAt(LocalDateTime.now());
        answerRepo.save(resp);

        convite.setStatus("ANSWERED");
        inviteRepo.save(convite);
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
