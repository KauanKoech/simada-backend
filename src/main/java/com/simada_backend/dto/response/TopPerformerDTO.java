package com.simada_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record TopPerformerDTO(
    @JsonProperty("nome_atleta") String nomeAtleta,
    @JsonProperty("foto") String foto,
    @JsonProperty("data_atualizacao") LocalDateTime dataAtualizacao,
    @JsonProperty("pontuacao") Double pontuacao,
    @JsonProperty("ultima_pontuacao") Double ultimaPontuacao
){}
