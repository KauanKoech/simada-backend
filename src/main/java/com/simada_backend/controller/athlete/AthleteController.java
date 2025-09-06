package com.simada_backend.controller.athlete;

import com.simada_backend.dto.request.athlete.UpdateAthleteRequest;
import com.simada_backend.dto.response.athlete.AthleteDetailDTO;
import com.simada_backend.service.athlete.AthleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coach")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AthleteController {

    private final AthleteService athleteService;

    @GetMapping("/{coachId}/athletes/{athleteId}")
    public AthleteDetailDTO getOne(
            @PathVariable Long coachId,
            @PathVariable Long athleteId
    ) {
        return athleteService.getAthlete(coachId, athleteId);
    }

    @PutMapping("/{coachId}/update/athlete/{athleteId}")
    public AthleteDetailDTO updateOne(
            @PathVariable Long coachId,
            @PathVariable Long athleteId,
            @RequestBody UpdateAthleteRequest body
    ) {
        return athleteService.updateAthlete(coachId, athleteId, body);
    }

    @DeleteMapping("/athlete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        athleteService.deleteAthlete(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
