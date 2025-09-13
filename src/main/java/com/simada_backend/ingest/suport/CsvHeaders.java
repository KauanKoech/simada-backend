package com.simada_backend.ingest.suport;
import java.util.List;

public enum CsvHeaders {
    SESSION(List.of("session")),
    DATE(List.of("date")),
    DORSAL(List.of("dorsal")),
    PLAYER(List.of("player")),
    TIME(List.of("time")), // duração total da sessão
    TOTAL_DISTANCE(List.of("total_distance")),
    MINUTE_DISTANCE(List.of("minute_distance")),
    VRANGE1(List.of("distance_vrange1")),
    VRANGE2(List.of("distance_vrange2")),
    VRANGE3(List.of("distance_vrange3")),
    VRANGE4(List.of("distance_vrange4")),
    VRANGE5(List.of("distance_vrange5")),
    VRANGE6(List.of("distance_vrange6")),
    MAX_SPEED(List.of("max_speed")),
    AVG_SPEED(List.of("average_speed")),
    NUM_DEC_EXPL(List.of("num_dec_expl")),
    MAX_DEC(List.of("max_dec")),
    NUM_ACC_EXPL(List.of("num_acc_expl")),
    MAX_ACC(List.of("max_acc")),
    PLAYER_LOAD(List.of("player_load")),
    HMLD(List.of("hmld")),
    HMLD_COUNT(List.of("hmld_count")),
    HMLD_REL(List.of("hmld_relative")),
    HMLD_TIME(List.of("hmld_time")),
    HID_INTERVALS(List.of("hid_intervals")),
    NUM_HIDS(List.of("num_hids")),
    HSR(List.of("hsr")),
    SPRINTS(List.of("sprints")),         // costuma ser contagem
    NUM_HSR(List.of("num_hsr")),
    TIME_VRANGE4(List.of("time_vrange4")),
    RPE(List.of("rpe"));

    public final List<String> aliases;
    CsvHeaders(List<String> aliases) { this.aliases = aliases; }
}