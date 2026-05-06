package com.example.digital_asset_risk_platform.outbox.mapper;

import com.example.digital_asset_risk_platform.event.config.KafkaTopicConfig;
import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import com.example.digital_asset_risk_platform.outbox.domain.OutboxEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventMapper {

    private final ObjectMapper objectMapper;

    public OutboxEvent toOutboxEvent(Object event) {
        if (event instanceof WithdrawalRequestedEvent withdrawalEvent) {
            return new OutboxEvent(
                    withdrawalEvent.eventId(),
                    "WithdrawalRequestEvent",
                    KafkaTopicConfig.WITHDRAWAL_REQUESTED,
                    String.valueOf(withdrawalEvent.withdrawalId()),
                    toJson(withdrawalEvent),
                    withdrawalEvent.occurredAt()
            );
        }

        if (event instanceof RiskEvaluationCompletedEvent evaluationEvent) {
            return new OutboxEvent(
                    evaluationEvent.eventId(),
                    "RiskEvaluationCreatedEvent",
                    KafkaTopicConfig.RISK_EVALUATION_COMPLETED,
                    String.valueOf(evaluationEvent.refId()),
                    toJson(evaluationEvent),
                    evaluationEvent.occurredAt()
            );
        }

        if (event instanceof RiskCaseCreatedEvent caseEvent) {
            return new OutboxEvent(
                    caseEvent.eventId(),
                    "RiskCaseCreatedEvent",
                    KafkaTopicConfig.RISK_CASE_CREATED,
                    String.valueOf(caseEvent.caseId()),
                    toJson(caseEvent),
                    caseEvent.occurredAt()
            );
        }

        throw new IllegalArgumentException("지원하지 않는 Outbox 이벤트 타입입니다: " + event.getClass().getName());
    }

    private String toJson(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox 이벤트 JSON 변환에 실패했습니다.", e);
        }
    }
}
