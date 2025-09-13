package com.simada_backend.service.loadCalc;


import com.simada_backend.model.loadCalc.WeeklyLoadResponse;
import com.simada_backend.model.loadCalc.WeeklyLoadRow;
import com.simada_backend.repository.loadCalc.WeeklyLoadQueryRepository;
import com.simada_backend.utils.Labels;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class LoadMetricsService {

    private final WeeklyLoadQueryRepository repo;

    public LoadMetricsService(WeeklyLoadQueryRepository repo) {
        this.repo = repo;
    }

    /**
     * Retorna o último dia da Quad-Week que começa em 1, 8, 15 ou 22.
     */
    public static LocalDate qwEnd(LocalDate qwStart) {
        int dom = qwStart.getDayOfMonth();
        // QW1: 1–7, QW2: 8–14, QW3: 15–21  => sempre +6 dias
        if (dom == 1 || dom == 8 || dom == 15) {
            return qwStart.plusDays(6);
        }
        // QW4: 22–fim do mês
        if (dom == 22) {
            YearMonth ym = YearMonth.of(qwStart.getYear(), qwStart.getMonth());
            return ym.atEndOfMonth();
        }
        throw new IllegalArgumentException("qwStart must be on day 1, 8, 15 or 22");
    }

    public WeeklyLoadResponse qwSma4(Long athleteId, LocalDate from, LocalDate to, Set<String> metrics) {
//        LocalDate fromQw = normalizeToQwStart(from);
//        LocalDate toQw = normalizeToQwStart(to);

        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must be before or equal to 'to'");
        }

        if (metrics.contains("acwr")) {
            int havePrevAtEnd = repo.countPreviousQuads(athleteId, to);
            if (havePrevAtEnd < 4) {

            }
        }

        var rowsDb = repo.qwWindow(athleteId, from, to);
        List<WeeklyLoadRow> rows = new ArrayList<>();

        for (var r : rowsDb) {
            var warnings = new ArrayList<String>();

            BigDecimal acwr = metrics.contains("acwr") ? r.getAcwr() : null;
            String acwrLabel = metrics.contains("acwr") ? Labels.acwrLabel(toDouble(acwr)) : null;

            BigDecimal pctQwUp = metrics.contains("pctqwup") ? r.getPctQwUp() : null;
            String pctQwUpLabel = metrics.contains("pctqwup") ? Labels.pctQwUpLabel(toDouble(pctQwUp)) : null;

            BigDecimal monotony = metrics.contains("monotony") ? r.getMonotony() : null;
            String monotonyLabel = metrics.contains("monotony") ? Labels.monotonyLabel(toDouble(monotony)) : null;

            BigDecimal strain = metrics.contains("strain") ? r.getStrain() : null;
            String strainLabel = metrics.contains("strain") ? Labels.strainLabel(toDouble(strain)) : null;

            if (metrics.contains("monotony") || metrics.contains("strain")) {
                if (r.getSdDaily() == null || r.getSdDaily().compareTo(BigDecimal.ZERO) == 0 ||
                        (r.getDaysWithLoad() != null && r.getDaysWithLoad() < 2)) {
                    warnings.add("monotony_strain_unavailable");
                }
            }

            // (Opcional) Se ACWR veio null, sinalize também:
            if (metrics.contains("acwr") && acwr == null) {
                warnings.add("acwr_unavailable_history_lt4");
            }

            rows.add(new WeeklyLoadRow(
                    r.getAthleteId(),
                    r.getQwStart(),
                    metrics.contains("ca") ? r.getCa() : null,
                    metrics.contains("cc") ? r.getCc() : null,
                    acwr,
                    acwrLabel,
                    pctQwUp,
                    pctQwUpLabel,
                    monotony,
                    monotonyLabel,
                    strain,
                    strainLabel,
                    r.getDaysWithLoad(),
                    warnings
            ));
        }
        return new WeeklyLoadResponse("sma4_qw", rows);
    }


//    private static LocalDate normalizeToQwStart(LocalDate d) {
//        int day = d.getDayOfMonth();
//        int startDay = (day <= 7) ? 1 : (day <= 14) ? 8 : (day <= 21) ? 15 : 22;
//        return LocalDate.of(d.getYear(), d.getMonth(), startDay);
//    }

    private static Double toDouble(BigDecimal v) {
        return v == null ? null : v.doubleValue();
    }

    public static class InsufficientHistoryException extends RuntimeException {
        public final ApiError payload;

        public InsufficientHistoryException(ApiError payload) {
            this.payload = payload;
        }
    }
}