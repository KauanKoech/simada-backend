package com.simada_backend.controller.athlete;

import com.simada_backend.dto.response.athlete.PeerAthleteDTO;
import com.simada_backend.service.athlete.PeerAthleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/athletes")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class PeerAthleteController {

    private final PeerAthleteService service;

    // GET /api/athletes/{athleteId}/peers
    @GetMapping("/{athleteId}/peers")
    public List<PeerAthleteDTO> peers(@PathVariable Long athleteId,
                                      @RequestParam(name="includeSelf", defaultValue = "true") boolean includeSelf) {
        return service.listPeers(athleteId, includeSelf);
    }
}