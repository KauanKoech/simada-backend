package com.simada_backend.service.session;

import com.simada_backend.dto.request.session.UpdateSessionRequest;
import com.simada_backend.dto.response.SessionDTO;
import com.simada_backend.model.athlete.Athlete;
import com.simada_backend.model.session.Metrics;
import com.simada_backend.model.session.Session;
import com.simada_backend.model.session.TrainingLoadAlert;
import com.simada_backend.repository.alert.TrainingLoadAlertRepository;
import com.simada_backend.service.loadCalculator.*;
import com.simada_backend.repository.athlete.AthleteRepository;
import com.simada_backend.repository.session.MetricsRepository;
import com.simada_backend.repository.session.CoachSessionsRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import jakarta.transaction.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SessionMetricsService {

    private final SessionLoadRepo sessionLoadRepo;
    private final CoachSessionsRepository sessionsRepo;
    private final MetricsRepository metricasRepo;
    private final AthleteRepository atletaRepo;
    private final WeeklyLoadQueryRepository weeklyLoadQueryRepository;
    private final TrainingLoadAlertRepository trainingLoadAlertRepository;


    @Transactional
    public void importMetricsFromCsv(int sessionId, MultipartFile file) throws CsvParsingException {
        // 1) Carrega a sessão
        Session session = sessionsRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sessão não encontrada"));

        Long coachId = session.getCoach().getId();

        // 2) Lê todo o arquivo em memória (simples e prático; p/ arquivos muito grandes, stream por linhas)
        final String content;
        try (InputStream in = file.getInputStream()) {
            content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falha ao ler arquivo", e);
        }

        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo vazio");
        }

        // 3) Detecta delimitador
        char delimiter = detectDelimiter(firstNonEmptyLine(content));
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(delimiter)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .setIgnoreSurroundingSpaces(true)
                .build();

        // 4) Faz o parse com cabeçalho
        try (CSVParser parser = CSVParser.parse(content, format)) {
            Map<String, Integer> hdr = parser.getHeaderMap();
            if (hdr == null || hdr.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cabeçalho não encontrado no CSV");
            }

            // Mapeia nomes de colunas (aceita PT/EN)
            String COL_PLAYER = pick(hdr, "player", "jogador", "atleta", "nome", "full_name");
            String COL_DORSAL = pick(hdr, "dorsal", "shirt_number", "numero_camisa", "camisa");
            String COL_TIME = pick(hdr, "time", "tempo", "minutos");
            String COL_DATE = pick(hdr, "date", "data");
            String COL_TDIST = pick(hdr, "total_distance", "dist_total", "distancia_total");
            String COL_MINDIST = pick(hdr, "minute_distance", "dist_minuto", "distancia_minuto");
            String COL_V1 = pick(hdr, "distance_vrange1", "vrange1", "dist_v1");
            String COL_V2 = pick(hdr, "distance_vrange2", "vrange2", "dist_v2");
            String COL_V3 = pick(hdr, "distance_vrange3", "vrange3", "dist_v3");
            String COL_V4 = pick(hdr, "distance_vrange4", "vrange4", "dist_v4");
            String COL_V5 = pick(hdr, "distance_vrange5", "vrange5", "dist_v5");
            String COL_V6 = pick(hdr, "distance_vrange6", "vrange6", "dist_v6");
            String COL_VMAX = pick(hdr, "max_speed", "vel_max");
            String COL_VAVG = pick(hdr, "average_speed", "vel_media");
            String COL_DEC_N = pick(hdr, "num_dec_expl", "num_deceleracoes");
            String COL_DEC_MAX = pick(hdr, "max_dec", "dec_max");
            String COL_ACC_N = pick(hdr, "num_acc_expl", "num_aceleracoes");
            String COL_ACC_MAX = pick(hdr, "max_acc", "acc_max");
            String COL_PL = pick(hdr, "player_load", "carga_jogador", "pl");
            String COL_HMLD = pick(hdr, "hmld", "high_metabolic_load_distance", "dist_hmld");
            String COL_HMLD_C = pick(hdr, "hmld_count", "cont_hmld");
            String COL_HMLD_R = pick(hdr, "hmld_relative", "hmld_relativo");
            String COL_HMLD_T = pick(hdr, "hmld_time", "tempo_hmld");
            String COL_HID_INT = pick(hdr, "hid_intervals");
            String COL_HIDS_N = pick(hdr, "num_hids");
            String COL_HSR = pick(hdr, "hsr", "high_speed_running");
            String COL_SPRINTS = pick(hdr, "sprints");
            String COL_HSR_N = pick(hdr, "num_hsr");
            String COL_TVR4 = pick(hdr, "time_vrange4", "tempo_v4");
            String COL_RPE = pick(hdr, "rpe", "esforco");

            // Essas duas são obrigatórias
            if (COL_PLAYER == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coluna 'player' (nome do atleta) não encontrada");

            List<Metrics> batch = new ArrayList<>();
            Set<Long> distinctAthleteIds = new HashSet<>();
            int row = 1;

            for (CSVRecord rec : parser) {
                row++;

                String playerName = get(rec, COL_PLAYER);
                Integer dorsal = tryParseInt(get(rec, COL_DORSAL));

                if (playerName == null || playerName.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Linha " + row + ": 'player' vazio");
                }

                //Resolve atleta (repo -> por nome+dorsal, nome, dorsal)
                Athlete athlete = resolveAtletaRepo(playerName, dorsal);
                if (athlete == null) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Linha " + row + ": atleta '" + playerName +
                                    "' (camisa " + (dorsal != null ? dorsal : "?") + ") não encontrado no banco de dados"
                    );
                }

                // Garante que o atleta pertence ao mesmo treinador da sessão
                if (athlete.getCoach() == null || !Objects.equals(athlete.getCoach().getId(), coachId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Linha " + row + ": atleta '" + playerName +
                                    "' não pertence ao treinador da sessão (id=" + coachId + ")");
                }

                // 6) Monta a entidade Métricas
                Metrics m = new Metrics();
                m.setSession(session);
                m.setAthlete(athlete);

                m.setTime(tryParseDoublePt(get(rec, COL_TIME)));
                m.setDate(tryParseLocalDate(get(rec, COL_DATE)));
                m.setTotalDistance(tryParseDoublePt(get(rec, COL_TDIST)));
                m.setMinuteDistance(tryParseDoublePt(get(rec, COL_MINDIST)));
                m.setDistanceVrange1(tryParseDoublePt(get(rec, COL_V1)));
                m.setDistanceVrange2(tryParseDoublePt(get(rec, COL_V2)));
                m.setDistanceVrange3(tryParseDoublePt(get(rec, COL_V3)));
                m.setDistanceVrange4(tryParseDoublePt(get(rec, COL_V4)));
                m.setDistanceVrange5(tryParseDoublePt(get(rec, COL_V5)));
                m.setDistanceVrange6(tryParseDoublePt(get(rec, COL_V6)));
                m.setMaxSpeed(tryParseDoublePt(get(rec, COL_VMAX)));
                m.setAverageSpeed(tryParseDoublePt(get(rec, COL_VAVG)));
                m.setNumDecExpl(tryParseInt(get(rec, COL_DEC_N)));
                m.setMaxDec(tryParseDoublePt(get(rec, COL_DEC_MAX)));
                m.setNumAccExpl(tryParseInt(get(rec, COL_ACC_N)));
                m.setMaxAcc(tryParseDoublePt(get(rec, COL_ACC_MAX)));
                m.setPlayerLoad(tryParseDoublePt(get(rec, COL_PL)));
                m.setHmld(tryParseDoublePt(get(rec, COL_HMLD)));
                m.setHmldCount(tryParseInt(get(rec, COL_HMLD_C)));
                m.setHmldRelative(tryParseDoublePt(get(rec, COL_HMLD_R)));
                m.setHmldTime(tryParseDoublePt(get(rec, COL_HMLD_T)));
                m.setHidIntervals(tryParseInt(get(rec, COL_HID_INT)));
                m.setNumHids(tryParseInt(get(rec, COL_HIDS_N)));
                m.setHsr(tryParseDoublePt(get(rec, COL_HSR)));
                m.setSprints(tryParseInt(get(rec, COL_SPRINTS)));
                m.setNumHsr(tryParseInt(get(rec, COL_HSR_N)));
                m.setTimeVrange4(tryParseDoublePt(get(rec, COL_TVR4)));
                m.setRpe(tryParseDoublePt(get(rec, COL_RPE)));

                batch.add(m);
                distinctAthleteIds.add(athlete.getId());

                // 4) Loads
                LoadCalculator.Result calc = LoadCalculator.compute(m);

                Long sessionIdLong = session.getId() == null ? null : session.getId().longValue();
                Long athleteIdLong = athlete.getId();

                SessionLoad load = sessionLoadRepo
                        .findBySessionIdAndAthleteId(sessionIdLong, athleteIdLong)
                        .orElseGet(SessionLoad::new);

                load.setSessionId(sessionIdLong);
                load.setAthleteId(athleteIdLong);
                load.setLoadSrpe(calc.loadSrpe);
                load.setLoadPlSim(calc.loadPlSim);
                load.setLoadEffective(calc.loadEffective);
                load.setLoadSource(Enum.valueOf(LoadSource.class, calc.loadSource));
                load.setFormulaVersion(calc.formulaVersion);
                load.setParamsJson(calc.paramsJson);
                sessionLoadRepo.save(load);

            }

            // Persiste tudo
            metricasRepo.saveAll(batch);

            //Atualiza num_atletas na sessão
            session.setNumAthletes(distinctAthleteIds.size());
            sessionsRepo.save(session);

            final Long sessionIdLong = session.getId() == null ? null : session.getId().longValue();
            final Long coachIdLong = (session.getCoach() != null ? session.getCoach().getId() : null);

            System.out.println("Distict Athletes: " + distinctAthleteIds.size());
            for (Long aid : distinctAthleteIds) {
                LocalDate latest = weeklyLoadQueryRepository.findLatestQwStart(aid);
                if (latest == null) {
                    System.out.println("Sem histórico na view para athleteId=" + aid);
                    continue;
                }

                var rows = weeklyLoadQueryRepository.qwWindow(aid, latest, latest);
                System.out.println("Rows(" + aid + "," + latest + "): " + rows);
                System.out.println("Rows: " + rows);
                if (rows == null || rows.isEmpty()) continue;

                var r = rows.get(rows.size() - 1);

                // Classificar
                String acwrL = Labels.acwrLabel(toDouble(r.getAcwr()));
                String pctQwUpL = Labels.pctQwUpLabel(toDouble(r.getPctQwUp()));
                String monoL = Labels.monotonyLabel(toDouble(r.getMonotony()));
                String strainL = Labels.strainLabel(toDouble(r.getStrain()));

                boolean hasAlert =
                        isAttentionOrRisk(acwrL) ||
                                isAttentionOrRisk(pctQwUpL) ||
                                isAttentionOrRisk(monoL) ||
                                isAttentionOrRisk(strainL);

                if (!hasAlert) continue;

                // Evita duplicar alerta do mesmo atleta na mesma sessão
                boolean exists = trainingLoadAlertRepository
                        .findByAthleteIdAndSessionId(aid, sessionIdLong)
                        .isPresent();
                if (exists) continue;

                var alert = TrainingLoadAlert.builder()
                        .athleteId(aid)
                        .coachId(coachIdLong != null ? coachIdLong : 0L)
                        .sessionId(sessionIdLong != null ? sessionIdLong : 0L)
                        .qwStart(r.getQwStart())

                        .acwr(toDouble(r.getAcwr()))
                        .acwrLabel(acwrL)

                        .pctQwUp(toDouble(r.getPctQwUp()))
                        .pctQwUpLabel(pctQwUpL)

                        .monotony(toDouble(r.getMonotony()))
                        .monotonyLabel(monoL)

                        .strain(toDouble(r.getStrain()))
                        .strainLabel(strainL)
                        .createdAt(Instant.now())

                        .build();
                trainingLoadAlertRepository.save(alert);
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao processar CSV", e);
        }
    }

    private static Double toDouble(BigDecimal bd) {
        return bd != null ? bd.doubleValue() : null;
    }

    private static boolean isAttentionOrRisk(String label) {
        if (label == null) return false;
        return switch (label) {
            case "atenção", "risco", "alto_risco" -> true;
            default -> false;
        };
    }

    @Transactional
    public void updateSessionNotes(int sessionId, String description) {
        if (description == null) {
            description = "";
        }
        Session s = sessionsRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sessão não encontrada"));
        s.setDescription(description);
    }

    @Transactional
    public SessionDTO updateSession(int id, UpdateSessionRequest req) {
        Session s = sessionsRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sessão não encontrada"));

        // type: "Training" | "Game"  -> BD: "treino" | "jogo"
        if (req.type() != null && !req.type().isBlank()) {
            s.setSession_type(mapTypeDbToApp(req.type()));
        }

        // title
        if (req.title() != null) {
            String t = req.title().trim();
            if (t.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title não pode ser vazio");
            }
            s.setTitle(t);
        }

        // date: aceita "YYYY-MM-DD" ou ISO datetime
        if (req.date() != null && !req.date().isBlank()) {
            s.setDate(parseDate(req.date()));
        }

        // score/description/location (aceita null para apagar)
        if (req.score() != null) {
            s.setScore(req.score().isBlank() ? null : req.score());
        }
        if (req.description() != null) {
            s.setDescription(req.description());
        }
        if (req.location() != null) {
            s.setLocal(req.location());
        }

        Session saved = sessionsRepo.save(s);

        // monta resposta no formato do app (type em "Training"/"Game")
        return new SessionDTO(
                saved.getId().longValue(),
                saved.getCoach() != null ? saved.getCoach().getId() : null,
                saved.getCoach_Photo(),
                saved.getDate(),
                mapTypeDbToApp(saved.getSession_type()),
                saved.getTitle(),
                saved.getNumAthletes(),
                saved.getScore(),
                saved.getDescription(),
                saved.getLocal()
        );
    }

    /* ------------ helpers ------------- */

    private static String get(CSVRecord r, String col) {
        if (col == null) return null;
        String v = r.get(col);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private static String pick(Map<String, Integer> headerMap, String... candidates) {
        for (String c : candidates) {
            if (c != null && headerMap.containsKey(c)) return c;
            for (String k : headerMap.keySet()) {
                if (k.equalsIgnoreCase(c)) return k;
            }
        }
        return null;
    }

    private static String firstNonEmptyLine(String content) {
        try (BufferedReader br = new BufferedReader(new StringReader(content))) {
            String l;
            while ((l = br.readLine()) != null) {
                if (!l.isBlank()) return l;
            }
        } catch (IOException ignored) {
        }
        return content;
    }

    private static char detectDelimiter(String firstLine) {
        int commas = count(firstLine, ',');
        int semis = count(firstLine, ';');
        int tabs = count(firstLine, '\t');
        if (semis >= commas && semis >= tabs) return ';';
        if (tabs >= commas && tabs >= semis) return '\t';
        return ','; // default
    }

    private static int count(String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) if (s.charAt(i) == c) n++;
        return n;
    }

    private static Integer tryParseInt(String s) {
        try {
            return (s == null || s.isBlank()) ? null : Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Double tryParseDoublePt(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        if (t.contains(",")) {
            t = t.replace(".", "").replace(",", ".");
        }
        try {
            return Double.parseDouble(t);
        } catch (Exception e) {
            return null;
        }
    }

    private static final DateTimeFormatter[] DATE_PATTERNS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("dd/MM/uuuu"),
            DateTimeFormatter.ofPattern("M/d/uuuu"),
            DateTimeFormatter.ofPattern("MM/dd/uuuu"),
            DateTimeFormatter.ofPattern("M/dd/uuuu"),
            DateTimeFormatter.ofPattern("MM/d/uuuu"),
    };

    public static LocalDate tryParseLocalDate(String raw) throws CsvParsingException {
        String s = normalize(raw);
        for (DateTimeFormatter f : DATE_PATTERNS) {
            try {
                return LocalDate.parse(s, f);
            } catch (DateTimeParseException ignore) {
            }
        }
        throw new CsvParsingException("data inválida: " + raw);
    }

    private static String normalize(String s) {
        return s == null ? null : s.trim();
    }

    private Athlete resolveAtletaRepo(String playerName, Integer dorsal) {
        Athlete a = null;
        if (playerName != null && dorsal != null) {
            a = atletaRepo.findFirstByNameIgnoreCaseAndJerseyNumber(playerName, dorsal).orElse(null);
        }
        if (a == null && playerName != null) {
            a = atletaRepo.findFirstByNameIgnoreCase(playerName).orElse(null);
        }
        if (a == null && dorsal != null) {
            List<Athlete> byNumber = atletaRepo.findByJerseyNumber(dorsal);
            if (byNumber.size() == 1) a = byNumber.get(0);
        }
        if (a == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Atleta não encontrado: " + playerName + (dorsal != null ? (" (#" + dorsal + ")") : ""));
        }
        return a;
    }

    private String mapTypeDbToApp(String tipoDb) {
        if (tipoDb == null) return "Training";
        switch (tipoDb.toLowerCase()) {
            case "treino":
                return "Training";
            case "jogo":
            case "game":
                return "Game";
            default:
                return "Training";
        }
    }

    private LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e1) {
            try {
                // ISO datetime -> pega a data
                return OffsetDateTime.parse(raw).toLocalDate();
            } catch (DateTimeParseException e2) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Formato inválido para 'date' (use YYYY-MM-DD ou ISO datetime)"
                );
            }
        }
    }
}
