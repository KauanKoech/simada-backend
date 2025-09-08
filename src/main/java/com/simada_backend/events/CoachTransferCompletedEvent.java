package com.simada_backend.events;

public record CoachTransferCompletedEvent(
        Long sourceCoachId,
        String sourceCoachName,
        Long destCoachId,
        String destEmail,
        String destCoachName
) {}