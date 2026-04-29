package com.example.digital_asset_risk_platform.event.producer;

import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class KafkaEventRelay {

    private final RiskEventProducer riskEventProducer;

    @TransactionalEventListener
    public void handle(WithdrawalRequestedEvent event) {
        riskEventProducer.publishWithdrawalRequested(event);
    }

    @TransactionalEventListener
    public void handle(RiskEvaluationCompletedEvent event) {
        riskEventProducer.publishRiskEvaluationCompleted(event);
    }

    @TransactionalEventListener
    public void handle(RiskCaseCreatedEvent event) {
        riskEventProducer.publishRiskCaseCreated(event);
    }
}
