package com.simada_backend.controller.athlete;

import com.simada_backend.dto.response.athlete.AthleteDetailDTO;
import com.simada_backend.dto.response.athlete.home.CalendarEvent;
import com.simada_backend.dto.response.athlete.home.MatchInfo;
import com.simada_backend.dto.response.athlete.home.PerfHighlight;
import com.simada_backend.service.athlete.AthleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/athlete")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AthleteController {

    private final AthleteService athleteService;

    @GetMapping("/{athleteId}/home")
    public AthleteDetailDTO getAthleteSummary(
            @PathVariable Long athleteId
    ) {
        return athleteService.getAthlete(athleteId);
    }

    @GetMapping("/{athleteId}/home/performance")
    public PerfHighlight performance(@PathVariable Long athleteId) {
        return athleteService.getPerformance(athleteId);
    }

    @GetMapping("/{athleteId}/home/recent")
    public MatchInfo recent(@PathVariable Long athleteId) {
        return athleteService.getRecent(athleteId);
    }

    @GetMapping("/{athleteId}/home/next-match")
    public MatchInfo nextMatch(@PathVariable Long athleteId) {
        return athleteService.getNextMatch(athleteId);
    }

    @GetMapping("/{athleteId}/home/calendar")
    public List<CalendarEvent> calendar(@PathVariable Long athleteId,
                                        @RequestParam int year,
                                        @RequestParam int month) {
        return athleteService.getCalendar(athleteId, year, month);
    }
}
