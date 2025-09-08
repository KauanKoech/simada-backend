package com.simada_backend.controller.coach;

import com.simada_backend.dto.response.coach.CoachProfileDTO;
import com.simada_backend.service.coach.CoachProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/coach/profile")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class CoachProfileController {

    private final CoachProfileService service;

    @GetMapping("/{coachId}")
    public ResponseEntity<CoachProfileDTO> getCoachProfile(@PathVariable Long coachId) {
        return ResponseEntity.ok(service.getProfile(coachId));
    }

    @PutMapping("/{coachId}")
    public ResponseEntity<Void> updateCoachProfile(@PathVariable Long coachId,
                                                   @RequestBody CoachProfileDTO payload) {
        service.updateProfile(coachId, payload);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{coachId}/avatar")
    public ResponseEntity<?> uploadCoachAvatar(@PathVariable Long coachId,
                                               @RequestParam("file") MultipartFile file) throws Exception {
        String photoUrl = service.uploadAvatar(coachId, file);
        return ResponseEntity.ok(new PhotoResponse(photoUrl));
    }


    @PostMapping("/{coachId}/delete-or-transfer")
    public ResponseEntity<Void> deleteOrTransfer(@PathVariable Long coachId,
                                                 @RequestBody DeleteOrTransferRequest req) {
        service.deleteOrTransferCoachAccount(coachId, req == null ? null : req.transferToEmail());
        return ResponseEntity.noContent().build();
    }

    public record DeleteOrTransferRequest(String transferToEmail) {
    }

    private record PhotoResponse(String photoUrl) {
    }
}
