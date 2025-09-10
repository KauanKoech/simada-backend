package com.simada_backend.controller.athlete;

import com.simada_backend.dto.request.athlete.UpdateAthleteProfileRequest;
import com.simada_backend.dto.request.athlete.UpdatePasswordRequest;
import com.simada_backend.dto.response.athlete.AthleteProfileDTO;
import com.simada_backend.dto.response.athlete.UploadAvatarResponse;
import com.simada_backend.service.athlete.AthleteProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/athletes")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AthleteProfileController {

    private final AthleteProfileService service;

    @GetMapping("/{athleteId}/profile")
    public AthleteProfileDTO get(@PathVariable Long athleteId) {
        return service.getProfile(athleteId);
    }

    @PutMapping("/{athleteId}/profile")
    public void update(@PathVariable Long athleteId,
                       @RequestBody UpdateAthleteProfileRequest req) {
        service.updateProfile(athleteId, req);
    }

    @PostMapping("/{athleteId}/avatar")
    public UploadAvatarResponse upload(@PathVariable Long athleteId,
                                       @RequestParam("file") MultipartFile file) {
        return service.uploadAvatar(athleteId, file);
    }

    @PostMapping("/{athleteId}/password")
    public void changePassword(@PathVariable Long athleteId,
                               @RequestBody UpdatePasswordRequest req) {
        service.changePassword(athleteId, req);
    }
}
