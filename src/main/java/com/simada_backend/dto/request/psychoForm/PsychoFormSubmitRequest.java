package com.simada_backend.dto.request.psychoForm;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class PsychoFormSubmitRequest {
    private int srpe;
    private int fatigue;
    private int soreness;
    private int mood;
    private int energy;
}