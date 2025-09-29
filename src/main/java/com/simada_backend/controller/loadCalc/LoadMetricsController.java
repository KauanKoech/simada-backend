package com.simada_backend.controller.loadCalc;


import com.simada_backend.service.loadCalc.ApiError;
import com.simada_backend.service.loadCalc.LoadMetricsService;
import com.simada_backend.model.loadCalc.WeeklyLoadResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/coach/athletes/{athleteId}")
public class LoadMetricsController {

    private final LoadMetricsService service;

    public LoadMetricsController(LoadMetricsService service) {
        this.service = service;
    }

    @GetMapping("/risk-calculations")
    public WeeklyLoadResponse qw(
            @PathVariable Long athleteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String metrics
    ) {
        Set<String> mset = parseMetrics(metrics);
        return service.qwSma4(athleteId, from, to, mset);
    }

    // CSV – Quad-Week
    @GetMapping(value="/export/qw.csv", produces="text/csv")
    public void qwCsv(
            @PathVariable Long athleteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String metrics,
            @RequestParam(required = false, defaultValue = "semicolon") String colsep,
            @RequestParam(required = false, defaultValue = "comma") String decsep,
            @RequestParam(required = false, defaultValue = "2") int precision,
            HttpServletResponse response
    ) throws Exception {
        Set<String> mset = parseMetrics(metrics);
        var res = service.qwSma4(athleteId, from, to, mset);

        // separador de coluna (final para uso no lambda)
        final String colSep = switch (colsep.toLowerCase()) {
            case "comma" -> ",";
            case "tab" -> "\t";
            default -> ";"; // semicolon
        };

        // formato numérico
        var symbols = new java.text.DecimalFormatSymbols();
        symbols.setDecimalSeparator("dot".equalsIgnoreCase(decsep) ? '.' : ',');
        StringBuilder pattern = new StringBuilder("0");
        if (precision > 0) {
            pattern.append(".");
            for (int i = 0; i < precision; i++) pattern.append("0");
        }
        var df = new java.text.DecimalFormat(pattern.toString(), symbols);

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=qw_"+athleteId+"_"+from+"_"+to+".csv");

        try (PrintWriter w = response.getWriter()) {

            // ORDEM FIXA das possíveis colunas (só entra o que foi pedido em metrics)
            List<String> base = List.of("athlete_id","qw_start","qw_end");
            List<String> metricCols = new ArrayList<>();
            if (mset.contains("ca"))        { metricCols.add("ca"); }
            if (mset.contains("cc"))        { metricCols.add("cc"); }
            if (mset.contains("acwr"))      { metricCols.add("acwr"); metricCols.add("acwr_label"); }
            if (mset.contains("pctqwup"))   { metricCols.add("pct_qw_up"); metricCols.add("pct_qw_up_label"); }
            if (mset.contains("monotony"))  { metricCols.add("monotony"); metricCols.add("monotony_label"); }
            if (mset.contains("strain"))    { metricCols.add("strain"); metricCols.add("strain_label"); }
            List<String> tail = List.of("days_with_load","cc_method","warnings");

            List<String> header = new ArrayList<>(base.size() + metricCols.size() + tail.size());
            header.addAll(base);
            header.addAll(metricCols);
            header.addAll(tail);
            w.println(String.join(colSep, header));

            // linhas
            res.rows().forEach(r -> {
                List<String> vals = new ArrayList<>();
                vals.add(r.athleteId().toString());
                vals.add(r.qwStart().toString());
                vals.add(LoadMetricsService.qwEnd(r.qwStart()).toString()); // fim correto da QW

                if (mset.contains("ca"))        vals.add(fmt(r.ca(), df));
                if (mset.contains("cc"))        vals.add(fmt(r.cc(), df));
                if (mset.contains("acwr"))     { vals.add(fmt(r.acwr(), df));       vals.add(n(r.acwrLabel())); }
                if (mset.contains("pctqwup"))  { vals.add(fmt(r.pctQwUp(), df));    vals.add(n(r.pctQwUpLabel())); }
                if (mset.contains("monotony")) { vals.add(fmt(r.monotony(), df));   vals.add(n(r.monotonyLabel())); }
                if (mset.contains("strain"))   { vals.add(fmt(r.strain(), df));     vals.add(n(r.strainLabel())); }

                vals.add(r.daysWithLoad()==null ? "0" : Integer.toString(r.daysWithLoad()));
                vals.add(res.ccMethod());
                vals.add(String.join("|", r.warnings()));

                w.println(String.join(colSep, vals));
            });
        }
    }

    private static String fmt(Object o, java.text.DecimalFormat df) {
        if (o == null) return "";
        if (o instanceof Number n) return df.format(n.doubleValue());
        return o.toString();
    }
    private static String n(Object o){ return o==null? "": o.toString(); }


    private static Set<String> parseMetrics(String metrics) {
        if (metrics == null || metrics.isBlank()) {
            return Set.of("ca","cc","acwr","pctqwup","monotony","strain"); // default: todas
        }
        String[] parts = metrics.toLowerCase().split(",");
        return new HashSet<>(Arrays.asList(parts));
    }

    @ExceptionHandler(LoadMetricsService.InsufficientHistoryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInsufficient(LoadMetricsService.InsufficientHistoryException ex){
        return ex.payload;
    }
}