package com.simada_backend.service;

import com.simada_backend.model.Atleta;
import com.simada_backend.model.ConviteAtleta;
import com.simada_backend.model.InvitationStatus;
import com.simada_backend.model.Usuario;
import com.simada_backend.repository.UsuarioRepository;
import com.simada_backend.repository.athlete.AtletaRepository;
import com.simada_backend.repository.athlete.ConviteAtletaRepository;
import com.simada_backend.repository.trainer.TrainerAthletesRepository;
import com.simada_backend.repository.trainer.TrainerRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class InviteService {
    private final ConviteAtletaRepository invitationRepo;
    private final TrainerRepository trainerRepo;
    private final UsuarioRepository userRepo;
    private final AtletaRepository athleteRepo;
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

    public ConviteAtleta createOrReuse(Long trainerId, String email) {
        var trainer = trainerRepo.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        var existing = invitationRepo.findFirstByTrainer_IdAndEmailAndStatus(trainerId, email, InvitationStatus.PENDING)
                .orElse(null);

        if (existing != null && existing.getExpiresAt().isAfter(LocalDateTime.now())) {
            sendEmail(existing, trainer.getFullName());
            return existing;
        }

        var inv = new ConviteAtleta();
        inv.setTrainer(trainer);
        inv.setEmail(email);
        inv.setToken(newToken());
        inv.setStatus(InvitationStatus.PENDING);
        inv.setExpiresAt(LocalDateTime.now().plusDays(ttlDays));
        inv = invitationRepo.save(inv);

        sendEmail(inv, trainer.getFullName());
        return inv;
    }

    private void sendEmail(ConviteAtleta inv, String trainerName) {
        String url = appFrontUrl + "/signup?invite=" + inv.getToken();
        String html = """
                <div style="font-family:sans-serif">
                  <p>Você foi convidado por <b>%s</b> para se cadastrar no Wiko.</p>
                  <p>Clique para criar sua conta: <a href="%s">%s</a></p>
                  <p>Convite expira em %s.</p>
                </div>
                """.formatted(trainerName, url, url, inv.getExpiresAt());

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

    public record InviteInfo(String email, String trainerName) {
    }

    public InviteInfo validateToken(String token) {
        var inv = invitationRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite"));
        if (inv.getStatus() != InvitationStatus.PENDING) throw new IllegalStateException("Invite no longer valid");
        if (inv.getExpiresAt().isBefore(LocalDateTime.now())) {
            inv.setStatus(InvitationStatus.EXPIRED);
            invitationRepo.save(inv);
            throw new IllegalStateException("Invite expired");
        }
        return new InviteInfo(inv.getEmail(), inv.getTrainer().getFullName());
    }

    @Transactional
    public Long completeInvite(String token, String name, String passwordHash,
                               String phone, LocalDate birth, String position) {

        ConviteAtleta inv = invitationRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite token"));

        if (inv.getStatus().equals(InvitationStatus.ACCEPTED) || inv.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invite expired or already used");
        }

        if (userRepo.existsByEmail(inv.getEmail())) {
            throw new IllegalStateException("User already exists with this email");
        }

        // 1) Usuario
        Usuario u = new Usuario();
        u.setEmail(inv.getEmail());
        u.setNome(name);
        u.setSenha(passwordHash);
        u.setTelefone(phone);
        if (birth != null) u.setDataNascimento(birth);
        u.setTipoUsuario("atleta");
        u = userRepo.save(u);

        // 2) Atleta (minimize NOT NULLs)
        Atleta a = new Atleta();
        a.setUsuario(u);
        a.setNome(u.getNome());
        a.setModalidade("Futebol");
        a.setTreinador(inv.getTrainer());
        if (position != null && !position.isBlank()) {
            a.setPosicao(position);
        }
        a = athleteRepo.save(a);

        // 3) Convite
        inv.setStatus(InvitationStatus.ACCEPTED);
        inv.setAcceptedAt(LocalDateTime.now());
        invitationRepo.save(inv);

        return a.getIdAtleta();
    }
}