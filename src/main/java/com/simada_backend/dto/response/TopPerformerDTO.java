package com.simada_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record TopPerformerDTO(
    @JsonProperty("athlete_name") String athleteName,
    @JsonProperty("photo") String foto,
    @JsonProperty("update_date") LocalDateTime updateDate,
    @JsonProperty("score") Double Score,
    @JsonProperty("last_score") Double lastScore
){}
