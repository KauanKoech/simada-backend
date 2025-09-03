package com.simada_backend.controller.athlete;

import com.simada_backend.dto.request.athlete.InviteRequest;
import com.simada_backend.service.InviteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AthleteInviteController {
    private final InviteService inviteService;
    private final PasswordEncoder encoder;

    @PostMapping("/trainers/{trainerId}/athlete-invitations")
    public ResponseEntity<?> createInvite(@PathVariable Long trainerId, @RequestBody InviteRequest.CreateInviteReq req) {
        if (req.email == null || req.email.isBlank())
            return ResponseEntity.badRequest().body("{\"message\":\"Email is required\"}");
        var inv = inviteService.createOrReuse(trainerId, req.email);
        return ResponseEntity.status(201).body(new Object() {
            public final Long id = inv.getId();
            public final String email = inv.getEmail();
            public final String status = inv.getStatus().name();
            public final String expires_at = inv.getExpiresAt().toString();
        });
    }

    @GetMapping("/athletes/invitations/{token}")
    public ResponseEntity<?> validate(@PathVariable String token) {
        try {
            var info = inviteService.validateToken(token);
            return ResponseEntity.ok(new Object() {
                public final String email = info.email();
                public final String trainerName = info.trainerName();
            });
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new Object() {
                public final String message = "Invalid invite";
            });
        } catch (IllegalStateException e) {
            return ResponseEntity.status(410).body(new Object() {
                public final String message = e.getMessage();
            });
        }
    }

    @PostMapping("/auth/complete-invite")
    public ResponseEntity<?> complete(@RequestBody InviteRequest.CompleteInviteReq req) {
        try {
            if (req.getToken() == null || req.getName() == null || req.getPassword() == null) {
                return ResponseEntity.badRequest().body(new Object() { public final String message = "Missing fields"; });
            }

            String hash = encoder.encode(req.getPassword());
            var id = inviteService.completeInvite(req.getToken(), req.getName(), hash, req.getPhone(), req.getBirth(), req.getPosition());

            return ResponseEntity.status(201).body(new Object() { public final Long athleteId = id; });

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(new Object() { public final String message = e.getMessage(); });
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(new Object() { public final String message = e.getMessage(); });
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new Object() { public final String message = "Failed to complete invite"; });
        }
    }
}