package com.simada_backend.controller;

import com.simada_backend.dto.request.athlete.UpdateAthleteRequest;
import com.simada_backend.dto.response.athlete.AthleteDetailDTO;
import com.simada_backend.service.AthleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainer")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AthleteController {

    private final AthleteService athleteService;

    @GetMapping("/{trainerId}/athletes/{athleteId}")
    public AthleteDetailDTO getOne(
            @PathVariable Long trainerId,
            @PathVariable Long athleteId
    ) {
        return athleteService.getAthlete(trainerId, athleteId);
    }

    @PutMapping("/{trainerId}/update/athlete/{athleteId}")
    public AthleteDetailDTO updateOne(
            @PathVariable Long trainerId,
            @PathVariable Long athleteId,
            @RequestBody UpdateAthleteRequest body
    ) {
        return athleteService.updateAthlete(trainerId, athleteId, body);
    }

    @DeleteMapping("/athlete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        athleteService.deleteAthlete(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
