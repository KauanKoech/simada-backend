package com.simada_backend.service.athlete;

import com.simada_backend.dto.request.athlete.UpdateAthleteRequest;
import com.simada_backend.dto.response.athlete.AthleteDetailDTO;
import com.simada_backend.dto.response.athlete.AthleteExtraDTO;
import com.simada_backend.dto.response.athlete.home.CalendarEvent;
import com.simada_backend.dto.response.athlete.home.MatchInfo;
import com.simada_backend.dto.response.athlete.home.PerfHighlight;
import com.simada_backend.model.User;
import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.athlete.AthleteExtra;
import com.simada_backend.repository.UserRepository;
import com.simada_backend.repository.athlete.AthleteExtraRepository;
import com.simada_backend.repository.athlete.AthleteHomeRepository;
import com.simada_backend.repository.athlete.AthleteRepository;
import com.simada_backend.repository.athlete.AthleteInviteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AthleteService {

    private final AthleteRepository athleteRepo;
    private final AthleteHomeRepository athleteHomeRepo;

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_OFFSET_DATE_TIME;


    @Transactional
    public AthleteDetailDTO getAthlete(Long athleteId) {
        Athlete athlete = athleteRepo.findById(athleteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Athlete not found"));

        User u = athlete.getUser();
        AthleteExtra ex = athlete.getExtra();

        return new AthleteDetailDTO(
                athlete.getId(),
                athlete.getName(),
                u != null ? u.getEmail() : null,
                u != null ? u.getPhone() : null,
                u != null && u.getBirthDate() != null ? u.getBirthDate().toString() : null,
                u != null ? u.getPassword() : null,
                athlete.getJerseyNumber() != null ? String.valueOf(athlete.getJerseyNumber()) : null,
                athlete.getPosition(),
                ex == null ? null : new AthleteExtraDTO(
                        ex.getHeightCm(), ex.getWeightKg(), ex.getLeanMassKg(), ex.getFatMassKg(),
                        ex.getBodyFatPct(), ex.getDominantFoot(), ex.getNationality(), ex.getInjuryStatus()
                )
        );
    }

    public PerfHighlight getPerformance(Long athleteId) {
        PerfHighlight perf = athleteHomeRepo.findLatestPerf(athleteId);
        if (perf == null) return new PerfHighlight(0, 0);
        return perf;
    }

    public MatchInfo getRecent(Long athleteId) {
        LocalDate today = LocalDate.now(ZONE);
        var row = athleteHomeRepo.findRecentGameForAthlete(athleteId, today);
        if (row == null) return new MatchInfo(null,null, "No recent game", null);

        ZonedDateTime zdt = row.date().atStartOfDay(ZONE);
        String iso = ISO.format(zdt);
        String title = row.title() != null ? row.title() : "Game";
        String subtitle = row.opponent() != null ? "vs " + row.opponent() : null;
        return new MatchInfo(row.id(), iso, title, subtitle);
    }

    public MatchInfo getNextMatch(Long athleteId) {
        LocalDate today = LocalDate.now(ZONE);
        var row = athleteHomeRepo.findNextGameForAthlete(athleteId, today);
        if (row == null) return new MatchInfo(null, null, "No upcoming game", null);

        ZonedDateTime zdt = row.date().atStartOfDay(ZONE);
        String iso = ISO.format(zdt);
        String title = row.title() != null ? row.title() : "Game";
        String subtitle = row.opponent() != null ? "vs " + row.opponent() : null;

        return new MatchInfo(row.id(), iso, title, subtitle);
    }

    public List<CalendarEvent> getCalendar(Long athleteId, int year, int month) {
        LocalDate first = LocalDate.of(year, month, 1);
        LocalDate nextMonthFirst = first.plusMonths(1);

        var rows = athleteHomeRepo.findCalendarForAthlete(athleteId, first, nextMonthFirst);

        return rows.stream().map(r -> {
            ZonedDateTime zdt = r.date().atStartOfDay(ZONE);
            String iso = ISO.format(zdt);

            String label = (r.title() != null && !r.title().isBlank())
                    ? r.title()
                    : (r.type() != null && r.type().equalsIgnoreCase("GAME") ? "Game" : "Training");

            String type = (r.type() != null && r.type().equalsIgnoreCase("GAME")) ? "game" : "training";

            return new CalendarEvent(iso, label, type);
        }).toList();
    }

//    @Transactional
//    public AthleteDetailDTO updateAthlete(Long coachId, Long athleteId, UpdateAthleteRequest req) {
//        Athlete athlete = atletaRepo.findByIdAndCoach_Id(athleteId, coachId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atleta não encontrado para este treinador"));
//
//        if (req.getName() != null) athlete.setName(req.getName());
//        if (req.getPosition() != null) athlete.setPosition(req.getPosition());
//
//        User u = athlete.getUser();
//        if (u != null) {
//            if (req.getEmail() != null) u.setEmail(req.getEmail());
//            if (req.getPhone() != null) u.setPhone(req.getPhone());
//            if (req.getBirth() != null && !req.getBirth().isBlank()) {
//                try {
//                    u.setBirthDate(LocalDate.parse(req.getBirth()));
//                } catch (Exception e) {
//                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de birth inválido (use YYYY-MM-DD)");
//                }
//            }
//            usuarioRepo.save(u);
//        }
//
//        // Extra
//        if (req.getExtra() != null) {
//            AthleteExtra ex = extraRepo.findByAthlete_Id(athleteId).orElseGet(() -> {
//                AthleteExtra novo = new AthleteExtra();
//                novo.setAthlete(athlete);
//                return novo;
//            });
//
//            var e = req.getExtra();
//            ex.setHeightCm(e.height_cm());
//            ex.setWeightKg(e.weight_kg());
//            ex.setLeanMassKg(e.lean_mass_kg());
//            ex.setFatMassKg(e.fat_mass_kg());
//            ex.setBodyFatPct(e.body_fat_pct());
//            ex.setDominantFoot(e.dominant_foot());
//            ex.setNationality(e.nationality());
//            ex.setInjuryStatus(e.injury_status());
//
//            extraRepo.save(ex);
//            athlete.setExtra(ex);
//        }
//
//        atletaRepo.save(athlete);
//        return getAthlete(coachId, athleteId);
//    }
//
//    @Transactional
//    public void deleteAthlete(Long athleteId) {
//        Athlete a = atletaRepo.findById(athleteId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atleta não encontrada"));
//
//        atletaRepo.deleteById(athleteId);
//        extraRepo.deleteById(athleteId);
//        convRepo.deleteByEmail(a.getUser().getEmail());
//        usuarioRepo.delete(a.getUser());
//    }
}