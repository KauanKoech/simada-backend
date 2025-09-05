package com.simada_backend.service.psychoForm;

import com.simada_backend.dto.request.psychoForm.PsychoFormSubmitRequest;
import com.simada_backend.model.psychoForm.PsychoFormResposta;
import com.simada_backend.model.psychoForm.PsychoFormConvite;
import com.simada_backend.repository.athlete.AtletaRepository;
import com.simada_backend.repository.psychoForm.PsychoFormConviteRepository;
import com.simada_backend.repository.psychoForm.PsychoFormRespostaRepository;
import com.simada_backend.repository.session.MetricasRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PsychoFormService {

    private final PsychoFormConviteRepository conviteRepo;
    private final PsychoFormRespostaRepository respostaRepo;
    private final AtletaRepository atletaRepo;
    private final MetricasRepository metricasRepo;
    private final JavaMailSender mailSender;

    public PsychoFormService(PsychoFormConviteRepository conviteRepo,
                             PsychoFormRespostaRepository respostaRepo,
                             AtletaRepository atletaRepo,
                             MetricasRepository metricasRepo,
                             JavaMailSender mailSender) {
        this.conviteRepo = Objects.requireNonNull(conviteRepo);
        this.respostaRepo = Objects.requireNonNull(respostaRepo);
        this.atletaRepo = Objects.requireNonNull(atletaRepo);
        this.metricasRepo = Objects.requireNonNull(metricasRepo);
        this.mailSender = Objects.requireNonNull(mailSender);
    }

    public List<PsychoFormConvite> createConvites(Long trainerId, Long sessionId, String publicBaseUrl) {
        if (trainerId == null || sessionId == null) {
            throw new IllegalArgumentException("trainerId e sessionId são obrigatórios.");
        }

        List<Long> atletaIds = metricasRepo.findAthletesBySessionId(sessionId);
        if (atletaIds == null || atletaIds.isEmpty()) {
            throw new RuntimeException("Any athlete was found, you need to add them in the system.");
        }

        List<PsychoFormConvite> convites = new ArrayList<>();

        for (Long atletaId : atletaIds) {
            String email = atletaRepo.findEmailByAtletaId(atletaId)
                    .orElseThrow(() -> new RuntimeException("E-mail not found for athleteId: " + atletaId));

            PsychoFormConvite convite = new PsychoFormConvite();
            convite.setToken(UUID.randomUUID().toString());
            convite.setIdTreinador(trainerId);
            convite.setIdAtleta(atletaId);
            convite.setIdSessao(sessionId);
            convite.setEmail(email);
            convite.setStatus("PENDING");
            convite.setCreatedAt(LocalDateTime.now());
            convite.setExpiresAt(LocalDateTime.now().plusDays(2));

            conviteRepo.save(convite);
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

    public PsychoFormConvite validateToken(String token) {
        PsychoFormConvite convite = conviteRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido."));

        if (!"PENDING".equalsIgnoreCase(convite.getStatus())) {
            throw new RuntimeException("Este formulário já foi respondido ou expirou.");
        }
        if (convite.getExpiresAt() == null || convite.getExpiresAt().isBefore(LocalDateTime.now())) {
            convite.setStatus("EXPIRED");
            conviteRepo.save(convite);
            throw new RuntimeException("Token expirado.");
        }
        return convite;
    }

    public void submitForm(String token, PsychoFormSubmitRequest req) {
        PsychoFormConvite convite = conviteRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido."));

        if (!"PENDING".equalsIgnoreCase(convite.getStatus())) {
            throw new RuntimeException("Este formulário já foi respondido ou expirou.");
        }
        if (convite.getExpiresAt() == null || convite.getExpiresAt().isBefore(LocalDateTime.now())) {
            convite.setStatus("EXPIRED");
            conviteRepo.save(convite);
            throw new RuntimeException("Token expirado.");
        }

        PsychoFormResposta resp = new PsychoFormResposta();
        resp.setToken(token);
        resp.setIdAtleta(convite.getIdAtleta());
        resp.setIdSessao(convite.getIdSessao());
        resp.setSRPE(req.getSRPE());
        resp.setFatigue(req.getFatigue());
        resp.setSoreness(req.getSoreness());
        resp.setMood(req.getMood());
        resp.setEnergy(req.getEnergy());
        resp.setSubmittedAt(LocalDateTime.now());
        respostaRepo.save(resp);

        convite.setStatus("ANSWERED");
        conviteRepo.save(convite);
    }
}
