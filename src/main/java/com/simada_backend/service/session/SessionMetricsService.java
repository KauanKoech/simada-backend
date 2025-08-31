package com.simada_backend.service.session;

import com.simada_backend.model.Atleta;
import com.simada_backend.model.Metricas;
import com.simada_backend.model.Sessao;
import com.simada_backend.repository.athlete.AtletaRepository;
import com.simada_backend.repository.session.MetricasRepository;
import com.simada_backend.repository.session.TrainerSessionsRepository;
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
import java.util.*;

@Service
public class SessionMetricsService {

    private final TrainerSessionsRepository sessionsRepo;
    private final MetricasRepository metricasRepo;
    private final AtletaRepository atletaRepo;

    public SessionMetricsService(TrainerSessionsRepository sessionsRepo,
                                 MetricasRepository metricasRepo,
                                 AtletaRepository atletaRepo) {
        this.sessionsRepo = sessionsRepo;
        this.metricasRepo = metricasRepo;
        this.atletaRepo = atletaRepo;
    }

    @Transactional
    public void importMetricsFromCsv(int sessionId, MultipartFile file) {
        // 1) Carrega a sessão
        Sessao sessao = sessionsRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sessão não encontrada"));

        Long trainerId = sessao.getTreinador().getId(); // usado para validar atleta pertence ao treinador

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

            List<Metricas> batch = new ArrayList<>();
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
                Atleta atleta = resolveAtletaRepo(playerName, dorsal);

                // Garante que o atleta pertence ao mesmo treinador da sessão
                if (atleta.getTreinador() == null || !Objects.equals(atleta.getTreinador().getId(), trainerId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Linha " + row + ": atleta '" + playerName +
                                    "' não pertence ao treinador da sessão (id=" + trainerId + ")");
                }

                // 6) Monta a entidade Métricas
                Metricas m = new Metricas();
                m.setSessao(sessao);
                m.setAtleta(atleta);

                m.setTime(tryParseBigDecimalPt(get(rec, COL_TIME)));
                m.setTotalDistance(tryParseBigDecimalPt(get(rec, COL_TDIST)));
                m.setMinuteDistance(tryParseBigDecimalPt(get(rec, COL_MINDIST)));
                m.setDistanceVrange1(tryParseBigDecimalPt(get(rec, COL_V1)));
                m.setDistanceVrange2(tryParseBigDecimalPt(get(rec, COL_V2)));
                m.setDistanceVrange3(tryParseBigDecimalPt(get(rec, COL_V3)));
                m.setDistanceVrange4(tryParseBigDecimalPt(get(rec, COL_V4)));
                m.setDistanceVrange5(tryParseBigDecimalPt(get(rec, COL_V5)));
                m.setDistanceVrange6(tryParseBigDecimalPt(get(rec, COL_V6)));
                m.setMaxSpeed(tryParseBigDecimalPt(get(rec, COL_VMAX)));
                m.setAverageSpeed(tryParseBigDecimalPt(get(rec, COL_VAVG)));
                m.setNumDecExpl(tryParseInt(get(rec, COL_DEC_N)));
                m.setMaxDec(tryParseBigDecimalPt(get(rec, COL_DEC_MAX)));
                m.setNumAccExpl(tryParseInt(get(rec, COL_ACC_N)));
                m.setMaxAcc(tryParseBigDecimalPt(get(rec, COL_ACC_MAX)));
                m.setPlayerLoad(tryParseBigDecimalPt(get(rec, COL_PL)));
                m.setHmld(tryParseBigDecimalPt(get(rec, COL_HMLD)));
                m.setHmldCount(tryParseInt(get(rec, COL_HMLD_C)));
                m.setHmldRelative(tryParseBigDecimalPt(get(rec, COL_HMLD_R)));
                m.setHmldTime(tryParseBigDecimalPt(get(rec, COL_HMLD_T)));
                m.setHidIntervals(tryParseInt(get(rec, COL_HID_INT)));
                m.setNumHids(tryParseInt(get(rec, COL_HIDS_N)));
                m.setHsr(tryParseBigDecimalPt(get(rec, COL_HSR)));
                m.setSprints(tryParseInt(get(rec, COL_SPRINTS)));
                m.setNumHsr(tryParseInt(get(rec, COL_HSR_N)));
                m.setTimeVrange4(tryParseBigDecimalPt(get(rec, COL_TVR4)));
                m.setRpe(tryParseBigDecimalPt(get(rec, COL_RPE)));

                batch.add(m);
                distinctAthleteIds.add(atleta.getId_atleta());
            }

            // Persiste tudo
            metricasRepo.saveAll(batch);

            //Atualiza num_atletas na sessão
            sessao.setNumAtletas(distinctAthleteIds.size());
            sessionsRepo.save(sessao);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro ao processar CSV", e);
        }
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

    private static BigDecimal tryParseBigDecimalPt(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        if (t.contains(",")) {
            t = t.replace(".", "").replace(",", ".");
        }
        try {
            return new BigDecimal(t);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Busca por nome+dorsal > nome > dorsal (usando os métodos do seu AtletaRepository).
     */
    private Atleta resolveAtletaRepo(String playerName, Integer dorsal) {
        Atleta a = null;
        if (playerName != null && dorsal != null) {
            a = atletaRepo.findFirstByFullNameIgnoreCaseAndShirtNumber(playerName, dorsal).orElse(null);
        }
        if (a == null && playerName != null) {
            a = atletaRepo.findFirstByFullNameIgnoreCase(playerName).orElse(null);
        }
        if (a == null && dorsal != null) {
            List<Atleta> byNumber = atletaRepo.findByShirtNumber(dorsal);
            if (byNumber.size() == 1) a = byNumber.get(0);
        }
        if (a == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Atleta não encontrado: " + playerName + (dorsal != null ? (" (#" + dorsal + ")") : ""));
        }
        return a;
    }
}
