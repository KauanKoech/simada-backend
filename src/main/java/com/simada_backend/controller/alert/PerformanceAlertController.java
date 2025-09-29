package com.simada_backend.controller.alert;

import com.simada_backend.dto.response.alert.PerformanceAlertDTO;
import com.simada_backend.dto.response.alert.PerformanceAnswerDTO;
import com.simada_backend.service.alert.PerformanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/alerts")
@CrossOrigin(origins = "http://localhost:3000")
public class PerformanceAlertController {
    private final PerformanceService service;

    public PerformanceAlertController(PerformanceService service) {
        this.service = service;
    }

    @GetMapping("/training-load")
    public List<PerformanceAlertDTO> getTrainingLoadAlerts(
            @RequestParam Long coachId,
            @RequestParam(required = false) Long athleteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return service.getAlertsByCoach(coachId);
    }

    @GetMapping("/training-load/athlete/{athleteId}")
    public ResponseEntity<PerformanceAnswerDTO> getAnswer(
            @PathVariable Long athleteId
    ) {
        return ResponseEntity.ok(
                service.getBySessionAndAthlete(athleteId).orElse(null)
        );
    }

    @DeleteMapping("/performance/{alertId}/delete/{coachId}")
    public ResponseEntity<Void> delete(
            @PathVariable("alertId") Long alertId,
            @PathVariable("coachId") Long coachId
    ) {
        service.deleteAlert(alertId, coachId);
        return ResponseEntity.noContent().build(); // 204
    }
}
