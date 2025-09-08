package com.simada_backend.events;

import com.simada_backend.service.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CoachTransferListener {

    private final MailService mailService;

    public CoachTransferListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Value("${app.ui.base-url:http://localhost:3000}")
    private String uiBaseUrl;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransferCompleted(CoachTransferCompletedEvent ev) {
        mailService.sendCoachTransferConfirmation(
                ev.destEmail(),
                ev.sourceCoachName(),
                ev.destCoachName(),
                uiBaseUrl
        );
    }
}