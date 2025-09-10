package com.simada_backend.controller.athlete;

import com.simada_backend.dto.response.AthleteSessionDTO;
import com.simada_backend.service.athlete.AthleteSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/athletes")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AthleteSessionController {

    private final AthleteSessionService athleteSessionService;

    @GetMapping("/{athleteId}/sessions")
    public List<AthleteSessionDTO> getAthleteSession(@PathVariable Long athleteId){
        return athleteSessionService.getSessions(athleteId);
    }
}
