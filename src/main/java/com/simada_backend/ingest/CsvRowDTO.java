package com.simada_backend.ingest;

import java.time.LocalDate;

public class CsvRowDTO {

    // Identificação
    public String sessionLabel;   // -> Session.title e Metrics.sessionName
    public LocalDate date;        // -> Session.date e Metrics.date
    public String player;         // -> User.name, Athlete.name, Metrics.player
    public String dorsal;         // vem como texto, convertida para Integer -> Athlete.jerseyNumber, Metrics.dorsal
    public String position;       // opcional -> Athlete.position, Metrics.position

    // Tempo
    public Double durationMin;    // -> Metrics.time

    // Distâncias / velocidades
    public Double totalDistanceM;     // -> Metrics.totalDistance
    public Double minuteDistanceMpm;  // -> Metrics.minuteDistance
    public Double vrange1M;           // -> Metrics.distanceVrange1
    public Double vrange2M;           // -> Metrics.distanceVrange2
    public Double vrange3M;           // -> Metrics.distanceVrange3
    public Double vrange4M;           // -> Metrics.distanceVrange4
    public Double vrange5M;           // -> Metrics.distanceVrange5
    public Double vrange6M;           // -> Metrics.distanceVrange6

    public Double maxSpeedKmh;        // -> Metrics.maxSpeed
    public Double avgSpeedKmh;        // -> Metrics.averageSpeed

    // Acelerações / Desacelerações
    public Integer numDecExpl;        // -> Metrics.numDecExpl
    public Double maxDecMs2;          // -> Metrics.maxDec
    public Integer numAccExpl;        // -> Metrics.numAccExpl
    public Double maxAccMs2;          // -> Metrics.maxAcc

    // HMLD
    public Double hmldM;              // -> Metrics.hmld
    public Integer hmldCount;         // -> Metrics.hmldCount
    public Double hmldRelative;       // -> Metrics.hmldRelative
    public Double hmldTimeMin;        // -> Metrics.hmldTime

    // HIDs / HSR / Sprints
    public Double hidIntervals;       // -> Metrics.hidIntervals (convertemos para Integer)
    public Integer numHids;           // -> Metrics.numHids
    public Double hsrM;               // -> Metrics.hsr
    public Integer sprints;           // -> Metrics.sprints
    public Integer numHsr;            // -> Metrics.numHsr

    // Outros
    public Double timeVrange4Min;     // -> Metrics.timeVrange4
    public Double rpe;                // -> Metrics.rpe (na base é Double, mesmo que 0–10)
}
