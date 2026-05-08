package com.example.digital_asset_risk_platform.event.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String WITHDRAWAL_REQUESTED = "withdrawal.requested";
    public static final String RISK_EVALUATION_COMPLETED = "risk.evaluation.completed";
    public static final String RISK_CASE_CREATED = "risk.case.created";

    @Bean
    public NewTopic withdrawalRequestedTopic() {
        return TopicBuilder.name(WITHDRAWAL_REQUESTED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic riskEvaluationCompletedTopic() {
        return TopicBuilder.name(RISK_EVALUATION_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic riskCaseCreatedTopic() {
        return TopicBuilder.name(RISK_CASE_CREATED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
