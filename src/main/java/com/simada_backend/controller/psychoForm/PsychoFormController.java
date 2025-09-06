package com.simada_backend.controller.psychoForm;


import com.simada_backend.dto.request.psychoForm.PsychoFormCreateRequest;
import com.simada_backend.dto.request.psychoForm.PsychoFormSubmitRequest;
import com.simada_backend.dto.response.PsychoAnswerDTO;
import com.simada_backend.model.psychoForm.PsychoFormInvite;
import com.simada_backend.service.psychoForm.PsychoFormService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/psycho-form")
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

            String firstToken = null;
            if (!convites.isEmpty()) {
                firstToken = convites.get(0).getToken();
            }

            List<Map<String, Object>> sentTo = convites.stream()
                    .map(c -> Map.<String, Object>of(
                            "athleteId", c.getIdAthlete(),
                            "email", c.getEmail(),
                            "url", ((frontendBaseUrl != null && !frontendBaseUrl.isBlank())
                                    ? frontendBaseUrl
                                    : "http://localhost:3000") + "/psycho-form/" + c.getToken()
                    ))
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "token", firstToken,
                    "sentTo", sentTo
            ));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Falha ao gerar/enviar os convites."));
        }
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> validate(@PathVariable String token) {
        try {
            var convite = service.validateToken(token);
            return ResponseEntity.ok(Map.of(
                    "athleteId", convite.getIdAthlete(),
                    "coachId", convite.getIdCoach(),
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
    public List<PsychoAnswerDTO> getAnswers(@PathVariable Long sessionId){
        return service.getPsychoAnswersBySession(sessionId);
    }
}
