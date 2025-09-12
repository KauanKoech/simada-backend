package com.simada_backend.dto.request.psycho;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class PsychoFormCreateRequest {
    private Long athleteId;
    private Long sessionId;
}
