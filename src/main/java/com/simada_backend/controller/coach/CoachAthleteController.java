package com.simada_backend.controller.coach;

import com.simada_backend.dto.request.athlete.UpdateAthleteRequest;
import com.simada_backend.dto.response.athlete.AthleteDetailDTO;
import com.simada_backend.service.athlete.AthleteService;
import com.simada_backend.service.coach.CoachAthleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coach")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class CoachAthleteController {

    private final CoachAthleteService coachAthleteService;

    @GetMapping("/{coachId}/athletes/{athleteId}")
    public AthleteDetailDTO getOne(
            @PathVariable Long coachId,
            @PathVariable Long athleteId
    ) {
        return coachAthleteService.getAthlete(coachId, athleteId);
    }

    @PutMapping("/{coachId}/update/athlete/{athleteId}")
    public AthleteDetailDTO updateOne(
            @PathVariable Long coachId,
            @PathVariable Long athleteId,
            @RequestBody UpdateAthleteRequest body
    ) {
        return coachAthleteService.updateAthlete(coachId, athleteId, body);
    }

    @DeleteMapping("/athlete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coachAthleteService.deleteAthlete(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
