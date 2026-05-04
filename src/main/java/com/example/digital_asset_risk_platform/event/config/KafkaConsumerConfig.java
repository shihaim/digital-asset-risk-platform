package com.example.digital_asset_risk_platform.event.config;

import com.example.digital_asset_risk_platform.event.dto.RiskCaseCreatedEvent;
import com.example.digital_asset_risk_platform.event.dto.RiskEvaluationCompletedEvent;
import com.example.digital_asset_risk_platform.event.dto.WithdrawalRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, WithdrawalRequestedEvent> withdrawalRequestedKafkaListenerContainerFactory() {
        return factory(WithdrawalRequestedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RiskEvaluationCompletedEvent> riskEvaluationCompletedKafkaListenerContainerFactory() {
        return factory(RiskEvaluationCompletedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, RiskCaseCreatedEvent> riskCaseCreatedKafkaListenerContainerFactory() {
        return factory(RiskCaseCreatedEvent.class);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(Class<T> targetType) {
        JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(targetType);
        jsonDeserializer.addTrustedPackages("com.example.digital_asset_risk_platform.event.dto");
        jsonDeserializer.ignoreTypeHeaders();

        HashMap<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, jsonDeserializer);

        DefaultKafkaConsumerFactory<String, T> consumerFactory = new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                jsonDeserializer
        );

        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        return factory;
    }
}
