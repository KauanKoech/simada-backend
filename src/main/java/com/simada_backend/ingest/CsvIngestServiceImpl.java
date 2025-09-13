package com.simada_backend.ingest;

import com.simada_backend.model.Coach;
import com.simada_backend.model.User;
import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.session.Metrics;
import com.simada_backend.model.session.Session;
import com.simada_backend.repository.UserRepository;
import com.simada_backend.repository.athlete.AthleteRepository;
import com.simada_backend.repository.coach.CoachRepository;
import com.simada_backend.repository.session.MetricsRepository;
import com.simada_backend.repository.session.SessionRepository;
import com.simada_backend.model.loadCalc.LoadCalculator;
import com.simada_backend.model.loadCalc.LoadSource;
import com.simada_backend.model.loadCalc.SessionLoad;
import com.simada_backend.repository.loadCalc.SessionLoadRepo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class CsvIngestServiceImpl implements CsvIngestService {

    private final UserRepository userRepo;
    private final CoachRepository coachRepo;
    private final AthleteRepository athleteRepo;
    private final SessionRepository sessionRepo;
    private final MetricsRepository metricsRepo;
    private final SessionLoadRepo sessionLoadRepo;

    public CsvIngestServiceImpl(UserRepository userRepo,
                                CoachRepository coachRepo,
                                AthleteRepository athleteRepo,
                                SessionRepository sessionRepo,
                                MetricsRepository metricsRepo,
                                SessionLoadRepo sessionLoadRepo) {
        this.userRepo = userRepo;
        this.coachRepo = coachRepo;
        this.athleteRepo = athleteRepo;
        this.sessionRepo = sessionRepo;
        this.metricsRepo = metricsRepo;
        this.sessionLoadRepo = sessionLoadRepo;
    }

    @Override
    public IngestCsvResponseDTO ingestCsv(Long coachId, InputStream csvStream) {
        Coach coach = coachRepo.findById(coachId)
                .orElseThrow(() -> new IllegalArgumentException("Coach não encontrado: " + coachId));

        int total = 0, ok = 0, err = 0;
        List<String> sampleErrors = new ArrayList<>();
        CsvRowMapper mapper = new CsvRowMapper();

        try (Reader reader = new InputStreamReader(csvStream, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                     .withDelimiter(';')
                     .withFirstRecordAsHeader()
                     .withIgnoreSurroundingSpaces()
                     .parse(reader)) {

            ensureHeader(parser, "date");
            ensureHeader(parser, "player");
            ensureHeader(parser, "time");

            for (CSVRecord rec : parser) {
                total++;
                try {
                    CsvRowDTO row = mapper.map(rec);

                    // 1) USER (um por atleta, email determinístico)
                    String email = genAthleteEmail(row.player);
                    User user = userRepo.findByEmail(email).orElseGet(() -> {
                        User u = new User();
                        u.setName(row.player);
                        u.setEmail(email);
                        u.setPassword("123");               // em prod: encoder.encode("123")
                        u.setUserType("athlete");
                        u.setBirthDate(LocalDate.of(1970, 1, 1)); // <-- Java NÃO tem argumentos nomeados
                        return userRepo.save(u);            // aqui o banco gera u.getId()
                    });

// 2) ATHLETE (por coach + name)
                    Athlete athlete = athleteRepo
                            .findByCoach_IdAndNameIgnoreCase(coach.getId(), row.player)
                            .orElseGet(() -> {
                                Athlete a = new Athlete();
                                a.setName(row.player);
                                a.setCoach(coach);
                                a.setUser(user);

                                // 👇 GARANTE que o PK do athlete seja IGUAL ao id do user
                                a.setId(user.getId());

                                return a;                   // será salvo mais abaixo
                            });

// Se já existia e ainda não tem user, vincula
                    if (athlete.getUser() == null) {
                        athlete.setUser(user);
                    }

// Se já existia e o id diverge do user, trate como erro (não mude PK existente!)
                    if (athlete.getId() != null && !athlete.getId().equals(user.getId())) {
                        throw new IllegalStateException(
                                "Athlete existente com id diferente do user.id; ajuste manual necessário (athleteId="
                                        + athlete.getId() + ", userId=" + user.getId() + ")"
                        );
                    }

// jersey_number (dorsal) vem como String no CSV (ex: "12" ou "12,0")
                    Integer jersey = null;
                    if (row.dorsal != null && !row.dorsal.isBlank()) {
                        try {
                            jersey = (int) Math.round(
                                    Double.parseDouble(row.dorsal.replace(",", ".")) // <-- Java: replace(",", ".")
                            );
                        } catch (NumberFormatException ignore) { /* mantém null */ }
                    }
                    if (jersey != null) athlete.setJerseyNumber(jersey);

// salva (insert se novo com id = user.id; update se existente)
                    athlete = athleteRepo.save(athlete);

                    // 3) SESSION (por coach + date + title)
                    String normTitle = (row.sessionLabel == null) ? "" : row.sessionLabel.trim();
                    Session session = normTitle.isBlank()
                            ? sessionRepo.findAllByCoach_IdAndDate(coach.getId(), row.date).stream().findFirst()
                            .orElseGet(() -> {
                                Session s = new Session();
                                s.setCoach(coach);
                                s.setDate(row.date);
                                return sessionRepo.save(s);
                            })
                            : sessionRepo.findAllByCoach_IdAndDateAndTitle(coach.getId(), row.date, normTitle).stream().findFirst()
                            .orElseGet(() -> {
                                Session s = new Session();
                                s.setCoach(coach);
                                s.setDate(row.date);
                                s.setTitle(normTitle);
                                return sessionRepo.save(s);
                            });

                    // 4) METRICS (uma linha do CSV)
                    Metrics m = new Metrics();
                    m.setSession(session);
                    m.setAthlete(athlete);

                    m.setSessionName(row.sessionLabel);
                    m.setDate(row.date);
                    m.setDorsal(jersey);
                    m.setPlayer(row.player);
                    m.setTime(row.durationMin);

                    m.setTotalDistance(row.totalDistanceM);
                    m.setMinuteDistance(row.minuteDistanceMpm);
                    m.setDistanceVrange1(row.vrange1M);
                    m.setDistanceVrange2(row.vrange2M);
                    m.setDistanceVrange3(row.vrange3M);
                    m.setDistanceVrange4(row.vrange4M);
                    m.setDistanceVrange5(row.vrange5M);
                    m.setDistanceVrange6(row.vrange6M);

                    m.setMaxSpeed(row.maxSpeedKmh);
                    m.setAverageSpeed(row.avgSpeedKmh);

                    m.setNumDecExpl(row.numDecExpl);
                    m.setMaxDec(row.maxDecMs2);
                    m.setNumAccExpl(row.numAccExpl);
                    m.setMaxAcc(row.maxAccMs2);

                    m.setHmld(row.hmldM);
                    m.setHmldCount(row.hmldCount);
                    m.setHmldRelative(row.hmldRelative);
                    m.setHmldTime(row.hmldTimeMin);

                    m.setHidIntervals(row.hidIntervals == null ? null : row.hidIntervals.intValue());
                    m.setNumHids(row.numHids);

                    m.setHsr(row.hsrM);
                    m.setSprints(row.sprints);
                    m.setNumHsr(row.numHsr);

                    m.setTimeVrange4(row.timeVrange4Min);
                    m.setRpe(row.rpe);

                    metricsRepo.save(m);

                    // 5) LOADS
                    LoadCalculator.Result calc = LoadCalculator.compute(m);

                    SessionLoad load = sessionLoadRepo.findBySessionIdAndAthleteId(session.getId(), athlete.getId())
                            .orElseGet(SessionLoad::new);

                    load.setSessionId(session.getId());
                    load.setAthleteId(athlete.getId());
                    load.setLoadSrpe(calc.loadSrpe);
                    load.setLoadPlSim(calc.loadPlSim);
                    load.setLoadEffective(calc.loadEffective);
                    load.setLoadSource("SRPE".equalsIgnoreCase(calc.loadSource) ? LoadSource.SRPE : LoadSource.PL_SIM);
                    load.setFormulaVersion(calc.formulaVersion);
                    load.setParamsJson(calc.paramsJson);

                    sessionLoadRepo.save(load);

                    ok++;
                } catch (Exception lineEx) {
                    err++;
                    if (sampleErrors.size() < 20) {
                        sampleErrors.add("Linha " + rec.getRecordNumber() + ": " + lineEx.getMessage());
                    }
                }
            }

            return new IngestCsvResponseDTO(null, null, total, ok, err, sampleErrors);

        } catch (Exception ex) {
            if (sampleErrors.isEmpty()) sampleErrors.add(ex.getMessage());
            return new IngestCsvResponseDTO(null, null, total, ok, err, sampleErrors);
        }
    }

    private static String genAthleteEmail(String name) {
        String slug = (name == null ? "athlete" : name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        if (slug.isBlank()) slug = "athlete";
        return slug + "@simada.local";
    }

    private static void ensureHeader(CSVParser parser, String expected) {
        if (!parser.getHeaderMap().containsKey(expected)) {
            throw new IllegalArgumentException("Cabeçalho ausente: " + expected);
        }
    }
}

