package com.simada_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendCoachTransferConfirmation(String to, String oldCoachName, String newCoachName, String appUrl) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setTo(to);
            helper.setSubject("You have been nominated to be the new coach in WIKO");

            String html = """
                <div style="font-family:Arial, sans-serif;line-height:1.4">
                  <h2>coach ownership transfer completeD</h2>
                  <p>Olá,</p>
                  <p>You have been appointed as <b>the new responsible coach</b> at WIKO.</p>
                  <ul>
                    <li>Previous Coach: <b>%s</b></li>
                    <li>New Coach: <b>%s</b></li>
                  </ul>
                  <p>Log in to view athletes, sessions, and alerts now under your responsibility:</p>
                  <p><a href="%s" style="background:#1db954;color:#fff;padding:10px 14px;border-radius:6px;text-decoration:none">Acessar SIMADA</a></p>
                  <p style="color:#666;font-size:12px">If you do not recognize this action, please reply to this email.</p>
                </div>
            """.formatted(escape(oldCoachName), escape(newCoachName), appUrl);

            helper.setText(html, true);
            mailSender.send(mime);
        } catch (Exception e) {
            System.err.println("Failed sending transfer email: " + e.getMessage());
        }
    }

    private String escape(String s) { return s == null ? "" : s.replace("<","&lt;").replace(">","&gt;"); }
}
