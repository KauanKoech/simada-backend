package com.simada_backend.controller.alert;

import com.simada_backend.dto.request.alert.PerfAlertRecoRequest;
import com.simada_backend.dto.request.alert.PsyAlertRecoRequest;
import com.simada_backend.dto.response.psycho.PsyRecoResponseDTO;
import com.simada_backend.service.alert.AIRecommendationAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "Recommendations")
@RestController
@RequestMapping(path = "/coach", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class AIRecommendationsController {

    private final AIRecommendationAlertService service;

    @Operation(summary = "Generate psycho emotional recommendations for the athletes")
    @PostMapping(path = "/{coachId}/psy-form/{sessionId}/athletes/{athleteId}/recommendations",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PsyRecoResponseDTO>> recommendPsycho(
            @PathVariable Long sessionId,
            @PathVariable Long athleteId,
            @Valid @RequestBody PsyAlertRecoRequest body
    ) {
        return service.generatePsychoRecommendations(sessionId, athleteId, body)
                .map(text -> ResponseEntity.ok(new PsyRecoResponseDTO(text)));
    }

    @Operation(summary = "Generate performance recommendations for the athletes")
    @PostMapping(path = "/{coachId}/performance/{sessionId}/athletes/{athleteId}/recommendations",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PsyRecoResponseDTO>> recommendPerformance(
            @PathVariable Long coachId,
            @PathVariable Long sessionId,
            @PathVariable Long athleteId,
            @Valid @RequestBody PerfAlertRecoRequest body
    ) {
        return service.generatePerformanceRecommendations(sessionId, athleteId, body)
                .map(text -> ResponseEntity.ok(new PsyRecoResponseDTO(text)));
    }
}
