package com.simada_backend.controller.psycho;


import com.simada_backend.dto.request.psycho.PsychoFormCreateRequest;
import com.simada_backend.dto.request.psycho.PsychoFormSubmitRequest;
import com.simada_backend.dto.response.psycho.PsychoAnswerDTO;
import com.simada_backend.model.psycho.PsychoFormInvite;
import com.simada_backend.service.psycho.PsychoFormService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/psycho-form")
@CrossOrigin(origins = "http://localhost:3000")
public class PsychoFormController {

    private final PsychoFormService service;

    @Value("${app.front.url:}")
    private String frontendBaseUrl;

    public PsychoFormController(PsychoFormService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createForm(@RequestParam Long coachId,
                                        @RequestBody PsychoFormCreateRequest req) {
        try {
            if (req == null || req.getSessionId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "sessionId missing."));
            }

            List<PsychoFormInvite> convites = service.createConvites(coachId, req.getSessionId(), frontendBaseUrl);

            String firstToken = convites.isEmpty() ? null : convites.get(0).getToken();

            String base = (frontendBaseUrl != null && !frontendBaseUrl.isBlank())
                    ? frontendBaseUrl
                    : "http://localhost:3000";

            List<Map<String, Object>> sentTo = convites.stream()
                    .map(c -> Map.<String, Object>of(
                            "athleteId", c.getIdAthlete().getId(),   // <-- aqui!
                            "email", c.getEmail(),
                            "url", base + "/psycho-form/" + c.getToken()
                    ))
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "token", firstToken,
                    "sentTo", sentTo
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed generating/sending invites."));
        }
    }


    @GetMapping("/{token:.+}")
    public ResponseEntity<?> validate(@PathVariable String token) {
        try {
            log.info("validate() called with token={}", token);
            PsychoFormInvite convite = service.validateToken(token);
            log.info("validate() found invite id={}, status={}, expiresAt={}",
                    convite.getId(), convite.getStatus(), convite.getExpiresAt());
            return ResponseEntity.ok(Map.of(
                    "athleteId", convite.getIdAthlete().getId(),
                    "coachId", convite.getIdCoach().getId(),
                    "email", convite.getEmail()
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/{token}/submit")
    public ResponseEntity<?> submit(@PathVariable String token,
                                    @RequestBody PsychoFormSubmitRequest req) {
        try {
            service.submitForm(token, req);
            return ResponseEntity.ok(Map.of("message", "Form sent successfully."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed saving answers."));
        }
    }

    @GetMapping("/answers/sessions/{sessionId}")
    public List<PsychoAnswerDTO> getAnswers(@PathVariable Long sessionId) {
        return service.getPsychoAnswersBySession(sessionId);
    }
}
