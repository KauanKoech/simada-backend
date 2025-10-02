package com.simada_backend.service.athlete;

import com.simada_backend.api.error.BusinessException;
import com.simada_backend.api.error.ErrorCode;
import com.simada_backend.model.Coach;
import com.simada_backend.model.User;
import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.athlete.invite.AthleteInvite;
import com.simada_backend.model.athlete.invite.InviteStatus;
import com.simada_backend.repository.UserRepository;
import com.simada_backend.repository.athlete.AthleteRepository;
import com.simada_backend.repository.athlete.AthleteInviteRepository;
import com.simada_backend.repository.coach.CoachRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Slf4j
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
    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    private static final SecureRandom RNG = new SecureRandom();

    private String newToken() {
        byte[] buf = new byte[32];
        RNG.nextBytes(buf);
        return HexFormat.of().formatHex(buf);
    }

    public AthleteInvite createOrReuse(Long coachId, String email) {
        var coach = coachRepo.findById(coachId).orElseThrow(() ->
                new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, "Coach not found")
        );

        var existing = invitationRepo.findFirstByCoach_IdAndEmailAndStatus(coachId, email, InviteStatus.PENDING)
                .orElse(null);

        if (existing != null && existing.getExpiresAt().isAfter(LocalDateTime.now())) {
            safeSendEmail(existing, coach.getName());
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

            safeSendEmail(inv, coach.getName());
            return inv;
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.CONSTRAINT_VIOLATION, HttpStatus.CONFLICT,
                    "This athlete was already invited by the coach.");
        }
    }

    private void safeSendEmail(AthleteInvite inv, String coachName) {
        if (!mailEnabled) {
            log.warn("E-mail desabilitado (app.mail.enabled=false). Invite {}", inv.getToken());
            return;
        }
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(appFrontUrl)
                    .path("/signup")
                    .queryParam("invite", inv.getToken())
                    .build()
                    .toUriString();

            var fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String exp = inv.getExpiresAt().format(fmt);

            String html = """
                    <div style="font-family:sans-serif">
                      <p>Você foi convidado por <b>%s</b> para se registrar no WIKO.</p>
                      <p>Clique para criar sua conta: <a href="%s">%s</a></p>
                      <p>Convite expira em %s.</p>
                    </div>
                    """.formatted(coachName, url, url, exp);

            MimeMessage msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(inv.getEmail());
            helper.setSubject("WIKO Invite");
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (org.springframework.mail.MailException ex) {
            log.error("Falha ao enviar e-mail para {} (invite {}): {}", inv.getEmail(), inv.getToken(), ex.getMessage(), ex);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

//    private void sendEmail(AthleteInvite inv, String coachName) {
//        String url = UriComponentsBuilder
//                .fromHttpUrl(appFrontUrl)
//                .path("/signup")
//                .queryParam("invite", inv.getToken())
//                .build()
//                .toUriString();
//
//        String html = """
//                <div style="font-family:sans-serif">
//                  <p>Você foi convidado por <b>%s</b> para se registrar no WIKO.</p>
//                  <p>Clique para criar sua conta: <a href="%s">%s</a></p>
//                  <p>Convite expira em %s.</p>
//                </div>
//                """.formatted(coachName, url, url, inv.getExpiresAt());
//
//        try {
//            MimeMessage msg = mailSender.createMimeMessage();
//            var helper = new MimeMessageHelper(msg, true, "UTF-8");
//            helper.setFrom(from);
//            helper.setTo(inv.getEmail());
//            helper.setSubject("WIKO Invite");
//            helper.setText(html, true);
//            mailSender.send(msg);
//        } catch (Exception e) {
//            throw new BusinessException(
//                    ErrorCode.INTERNAL_ERROR,
//                    HttpStatus.INTERNAL_SERVER_ERROR,
//                    "Failed to send email"
//            );
//        }
//    }

    public record InviteInfo(String email, String coachName) {
    }

    public InviteInfo validateToken(String token) {
        var inv = invitationRepo.findByToken(token).orElseThrow(() ->
                new BusinessException(
                        ErrorCode.TOKEN_INVALID,
                        HttpStatus.BAD_REQUEST,
                        "Invalid invite token")
        );
        if (inv.getStatus() != InviteStatus.PENDING) {
            throw new BusinessException(
                    ErrorCode.TOKEN_INVALID,
                    HttpStatus.GONE,
                    "Invite no longer valid"
            );
        }
        if (inv.getExpiresAt().isBefore(LocalDateTime.now())) {
            inv.setStatus(InviteStatus.EXPIRED);
            invitationRepo.save(inv);
            throw new BusinessException(
                    ErrorCode.TOKEN_EXPIRED,
                    HttpStatus.GONE,
                    "Invite expired"
            );
        }
        return new InviteInfo(inv.getEmail(), inv.getCoach().getName());
    }

    @Transactional
    public Long completeInvite(String token, String name, String passwordHash,
                               String phone, LocalDate birth, String position) {

        var inv = invitationRepo.findByToken(token).orElseThrow(() ->
                new BusinessException(
                        ErrorCode.TOKEN_INVALID,
                        HttpStatus.BAD_REQUEST,
                        "Invalid invite token")
        );

        if (inv.getStatus() == InviteStatus.ACCEPTED) {
            throw new BusinessException(
                    ErrorCode.TOKEN_INVALID,
                    HttpStatus.GONE,
                    "Invite already used"
            );
        }
        if (inv.getExpiresAt().isBefore(LocalDateTime.now())) {
            inv.setStatus(InviteStatus.EXPIRED);
            invitationRepo.save(inv);
            throw new BusinessException(
                    ErrorCode.TOKEN_EXPIRED,
                    HttpStatus.GONE,
                    "Invite expired"
            );
        }
        if (userRepo.existsByEmail(inv.getEmail())) {
            throw new BusinessException(
                    ErrorCode.EMAIL_IN_USE,
                    HttpStatus.CONFLICT,
                    "User already exists with this email"
            );
        }

        //User
        User u = new User();
        u.setEmail(inv.getEmail());
        u.setName(name);
        u.setPassword(passwordHash);
        u.setPhone(phone);
        if (birth != null) u.setBirthDate(birth);
        u.setUserType("athlete");
        u = userRepo.save(u);

        Coach coach = coachRepo.getReferenceById(inv.getCoach().getId());

        //Athlete
        Athlete a = new Athlete();
        a.setUser(u);
        a.setId(u.getId());
        a.setName(u.getName());
        a.setCoach(coach);
        if (position != null && !position.isBlank()) {
            a.setPosition(position);
        }
        a = athleteRepo.save(a);

        //Invite
        inv.setStatus(InviteStatus.ACCEPTED);
        inv.setAcceptedAt(LocalDateTime.now());
        invitationRepo.save(inv);

        return a.getId();
    }
}