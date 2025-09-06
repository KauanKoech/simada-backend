package com.simada_backend.service.athlete;

import com.simada_backend.model.User;
import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.athlete.invite.AthleteInvite;
import com.simada_backend.model.athlete.invite.InviteStatus;
import com.simada_backend.repository.UserRepository;
import com.simada_backend.repository.athlete.AthleteRepository;
import com.simada_backend.repository.athlete.AthleteInviteRepository;
import com.simada_backend.repository.coach.CoachRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class InviteService {
    private final AthleteInviteRepository invitationRepo;
    private final CoachRepository coachRepo;
    private final UserRepository userRepo;
    private final AthleteRepository athleteRepo;
    private final JavaMailSender mailSender;

    @Value("${app.front.url}")
    private String appFrontUrl;
    @Value("${app.mail.from:no-reply@wiko.com}")
    private String from;
    @Value("${app.invite.ttl-days:7}")
    private int ttlDays;

    private static final SecureRandom RNG = new SecureRandom();

    private String newToken() {
        byte[] buf = new byte[32];
        RNG.nextBytes(buf);
        return HexFormat.of().formatHex(buf);
    }

    public AthleteInvite createOrReuse(Long coachId, String email) {
        var coach = coachRepo.findById(coachId)
                .orElseThrow(() -> new IllegalArgumentException("Coach not found"));

        var existing = invitationRepo.findFirstByCoach_IdAndEmailAndStatus(coachId, email, InviteStatus.PENDING)
                .orElse(null);

        if (existing != null && existing.getExpiresAt().isAfter(LocalDateTime.now())) {
            sendEmail(existing, coach.getName());
            return existing;
        }

        try {
            var inv = new AthleteInvite();
            inv.setCoach(coach);
            inv.setEmail(email);
            inv.setToken(newToken());
            inv.setStatus(InviteStatus.PENDING);
            inv.setExpiresAt(LocalDateTime.now().plusDays(ttlDays));
            inv = invitationRepo.save(inv);

            sendEmail(inv, coach.getName());
            return inv;
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Este atleta já foi convidado por este treinador."
            );
        }
    }

    private void sendEmail(AthleteInvite inv, String coachName) {
        String url = appFrontUrl + "/signup?invite=" + inv.getToken();
        String html = """
                <div style="font-family:sans-serif">
                  <p>Você foi convidado por <b>%s</b> para se cadastrar no Wiko.</p>
                  <p>Clique para criar sua conta: <a href="%s">%s</a></p>
                  <p>Convite expira em %s.</p>
                </div>
                """.formatted(coachName, url, url, inv.getExpiresAt());

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(inv.getEmail());
            helper.setSubject("Convite para o Wiko");
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public record InviteInfo(String email, String coachName) {
    }

    public InviteInfo validateToken(String token) {
        var inv = invitationRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite"));
        if (inv.getStatus() != InviteStatus.PENDING) throw new IllegalStateException("Invite no longer valid");
        if (inv.getExpiresAt().isBefore(LocalDateTime.now())) {
            inv.setStatus(InviteStatus.EXPIRED);
            invitationRepo.save(inv);
            throw new IllegalStateException("Invite expired");
        }
        return new InviteInfo(inv.getEmail(), inv.getCoach().getName());
    }

    @Transactional
    public Long completeInvite(String token, String name, String passwordHash,
                               String phone, LocalDate birth, String position) {

        AthleteInvite inv = invitationRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite token"));

        if (inv.getStatus().equals(InviteStatus.ACCEPTED) || inv.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invite expired or already used");
        }

        if (userRepo.existsByEmail(inv.getEmail())) {
            throw new IllegalStateException("User already exists with this email");
        }

        // 1) Usuario
        User u = new User();
        u.setEmail(inv.getEmail());
        u.setName(name);
        u.setPassword(passwordHash);
        u.setPhone(phone);
        if (birth != null) u.setBirthDate(birth);
        u.setUserType("atleta");
        u = userRepo.save(u);

        // 2) Atleta (minimize NOT NULLs)
        Athlete a = new Athlete();
        a.setUser(u);
        a.setName(u.getName());
        a.setCoach(inv.getCoach());
        if (position != null && !position.isBlank()) {
            a.setPosition(position);
        }
        a = athleteRepo.save(a);

        // 3) Convite
        inv.setStatus(InviteStatus.ACCEPTED);
        inv.setAcceptedAt(LocalDateTime.now());
        invitationRepo.save(inv);

        return a.getId();
    }
}