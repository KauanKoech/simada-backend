package com.simada_backend.service.loadCalculator;


import com.simada_backend.model.session.Metrics;

import java.math.BigDecimal;

public class LoadCalculator {

    public static class Result {
        public Double loadSrpe;     // pode ser null
        public Double loadPlSim;    // pode ser null
        public Double loadEffective;
        public String loadSource;  // "SRPE" ou "PL_SIM"
        public String paramsJson;  // {"alpha":1,"beta":3,"gamma":5}
        public String formulaVersion = "v1.0";
    }

    // α=1, β=3, γ=5 (fixos por enquanto)
    private static final double ALPHA = 0.111;
    private static final double BETA = 0.333;
    private static final double GAMMA = 0.553;

    public static Result compute(Metrics row) {
        Result r = new Result();

        // sRPE se RPE presente
        if (row.getRpe() != null && row.getTime() != null) {
            r.loadSrpe = row.getRpe() * row.getTime();
        }

        Double dist = nz(row.getTotalDistance());
        Integer acc = nz(row.getNumAccExpl());
        Integer dec = nz(row.getNumDecExpl());
        Integer sprints = nz(row.getSprints());
        r.loadPlSim = ALPHA * dist + BETA * (acc + dec) + GAMMA * sprints;

        if (r.loadSrpe != null) {
            r.loadEffective = r.loadSrpe;
            r.loadSource = "SRPE";
        } else {
            r.loadEffective = r.loadPlSim;
            r.loadSource = "PL_SIM";
        }

        r.paramsJson = "{\"alpha\":0.111,\"beta\":0.333,\"gamma\":0.553}";
        return r;
    }

    private static double nz(Double v) {
        return v == null ? 0d : v;
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
