package com.simada_backend.ingest;

import com.simada_backend.ingest.suport.CsvHeaders;
import com.simada_backend.ingest.suport.ParseUtils;
import com.simada_backend.service.loadCalc.CsvParsingException;
import org.apache.commons.csv.CSVRecord;

public class CsvRowMapper {
    public CsvRowDTO map(CSVRecord r) throws CsvParsingException {   // <<--- AQUI
        CsvRowDTO dto = new CsvRowDTO();

        dto.sessionLabel = get(r, CsvHeaders.SESSION);
        dto.player = require(r, CsvHeaders.PLAYER, "player é obrigatório"); // require lança CsvParsingException

        dto.dorsal = get(r, CsvHeaders.DORSAL);
        dto.date = ParseUtils.parseDateUSOrder(
                require(r, CsvHeaders.DATE, "date é obrigatório"));
        dto.durationMin = ParseUtils.parseDouble(
                require(r, CsvHeaders.TIME, "time (min) é obrigatório"));
        if (dto.durationMin == null || dto.durationMin <= 0) {
            throw new CsvParsingException("time inválido (deve ser > 0)");
        }

        dto.totalDistanceM = ParseUtils.parseDouble(get(r, CsvHeaders.TOTAL_DISTANCE));
        dto.minuteDistanceMpm = ParseUtils.parseDouble(get(r, CsvHeaders.MINUTE_DISTANCE));

        dto.vrange1M = ParseUtils.parseDouble(get(r, CsvHeaders.VRANGE1));
        dto.vrange2M = ParseUtils.parseDouble(get(r, CsvHeaders.VRANGE2));
        dto.vrange3M = ParseUtils.parseDouble(get(r, CsvHeaders.VRANGE3));
        dto.vrange4M = ParseUtils.parseDouble(get(r, CsvHeaders.VRANGE4));
        dto.vrange5M = ParseUtils.parseDouble(get(r, CsvHeaders.VRANGE5));
        dto.vrange6M = ParseUtils.parseDouble(get(r, CsvHeaders.VRANGE6));

        dto.maxSpeedKmh = ParseUtils.parseDouble(get(r, CsvHeaders.MAX_SPEED));
        dto.avgSpeedKmh = ParseUtils.parseDouble(get(r, CsvHeaders.AVG_SPEED));

        dto.numDecExpl = ParseUtils.parseInteger(get(r, CsvHeaders.NUM_DEC_EXPL));
        dto.maxDecMs2 = ParseUtils.parseDouble(get(r, CsvHeaders.MAX_DEC));
        dto.numAccExpl = ParseUtils.parseInteger(get(r, CsvHeaders.NUM_ACC_EXPL));
        dto.maxAccMs2 = ParseUtils.parseDouble(get(r, CsvHeaders.MAX_ACC));

        dto.hmldM = ParseUtils.parseDouble(get(r, CsvHeaders.HMLD));
        dto.hmldCount = ParseUtils.parseInteger(get(r, CsvHeaders.HMLD_COUNT));
        dto.hmldRelative = ParseUtils.parseDouble(get(r, CsvHeaders.HMLD_REL));
        dto.hmldTimeMin = ParseUtils.parseDouble(get(r, CsvHeaders.HMLD_TIME));

        // Se preferir inteiro aqui, troque para parseInteger
        dto.hidIntervals = ParseUtils.parseDouble(get(r, CsvHeaders.HID_INTERVALS));
        dto.numHids = ParseUtils.parseInteger(get(r, CsvHeaders.NUM_HIDS));

        dto.hsrM = ParseUtils.parseDouble(get(r, CsvHeaders.HSR));
        dto.sprints = ParseUtils.parseInteger(get(r, CsvHeaders.SPRINTS));
        dto.numHsr = ParseUtils.parseInteger(get(r, CsvHeaders.NUM_HSR));

        dto.timeVrange4Min = ParseUtils.parseDouble(get(r, CsvHeaders.TIME_VRANGE4));

        Integer rpeInt = ParseUtils.parseInteger(get(r, CsvHeaders.RPE));
        if (rpeInt != null && (rpeInt < 0 || rpeInt > 10)) {
            throw new CsvParsingException("rpe fora do intervalo 0..10");
        }
        dto.rpe = rpeInt == null ? null : rpeInt.doubleValue();

        return dto;
    }

    private static String get(CSVRecord r, CsvHeaders h) {
        for (String key : h.aliases) {
            if (r.isMapped(key)) {
                String v = r.get(key);
                if (v != null && !v.isBlank()) return v.trim();
            }
        }
        return null;
    }

    private static String require(CSVRecord r, CsvHeaders h, String msg) throws CsvParsingException {
        String v = get(r, h);
        if (v == null) throw new CsvParsingException(msg);
        return v;
    }
}