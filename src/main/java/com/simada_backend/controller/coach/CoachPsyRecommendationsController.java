package com.simada_backend.controller.coach;

import com.simada_backend.dto.request.psychoForm.PsyRecoRequest;
import com.simada_backend.dto.response.psycho.PsyRecoResponseDTO;
import com.simada_backend.service.psycho.PsyRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "Psy Recommendations")
@RestController
@RequestMapping(path = "/api/coach/psy-form", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class CoachPsyRecommendationsController {

    private final PsyRecommendationService service;

    @Operation(summary = "Generate psycho emotional recommendations for the athletes")
    @PostMapping(path = "/{sessionId}/athletes/{athleteId}/recommendations",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PsyRecoResponseDTO>> recommend(
            @PathVariable Long sessionId,
            @PathVariable Long athleteId,
            @Valid @RequestBody PsyRecoRequest body
    ) {
        return service.generateRecommendations(sessionId, athleteId, body)
                .map(text -> ResponseEntity.ok(new PsyRecoResponseDTO(text)));
    }
}
