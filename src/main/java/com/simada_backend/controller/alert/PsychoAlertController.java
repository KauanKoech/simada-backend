package com.simada_backend.controller.alert;

import com.simada_backend.dto.response.alert.PsychoAlertDTO;
import com.simada_backend.dto.response.psycho.PsychoAnswerAthleteDTO;
import com.simada_backend.service.alert.PsychoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/alerts")
@CrossOrigin(origins = "http://localhost:3000")
public class PsychoAlertController {

    private final PsychoService psychoService;

    public PsychoAlertController(PsychoService psychoService) {
        this.psychoService = psychoService;
    }

    @GetMapping("/psycho-risk/{coachId}")
    public List<PsychoAlertDTO> getPsychoRiskAlerts(@PathVariable("coachId") Long coachId) {
        return psychoService.getPsychoRiskAlerts(coachId);
    }

    @GetMapping("/sessions/{sessionId}/psy-form/answers/{athleteId}")
    public List<PsychoAnswerAthleteDTO> getPsychoAnswerByAthlete(
            @PathVariable("sessionId") Long sessionId,
            @PathVariable("athleteId") Long athleteId
    ) {
        return psychoService.getPsychoAnswerByAthlete(sessionId, athleteId);
    }

}
