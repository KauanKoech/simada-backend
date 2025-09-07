package com.simada_backend.controller;

import com.simada_backend.dto.response.PsychoAlertDTO;
import com.simada_backend.dto.response.PsychoAnswerAthleteDTO;
import com.simada_backend.service.AlertsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "http://localhost:3000")
public class AlertController {

    private final AlertsService alertsService;

    public AlertController(AlertsService alertsService) {
        this.alertsService = alertsService;
    }

    @GetMapping("/psycho-risk/{coachId}")
    public List<PsychoAlertDTO> getPsychoRiskAlerts(@PathVariable("coachId") Long coachId) {
        return alertsService.getPsychoRiskAlerts(coachId);
    }

    @GetMapping("/sessions/{sessionId}/psy-form/answers/{athleteId}")
    public List<PsychoAnswerAthleteDTO> getPsychoAnswerByAthlete(
            @PathVariable("sessionId") Long sessionId,
            @PathVariable("athleteId") Long athleteId
    ) {
        return alertsService.getPsychoAnswerByAthlete(sessionId, athleteId);
    }

//    @GetMapping("/performance/{coachId}")
//    public List<PerformanceAlertDTO> getPerformanceAlerts(@PathVariable("coachId") Long coachId) {
//        return alertsService.getPerformanceAlerts(coachId);
//    }
}
