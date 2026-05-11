package com.example.digital_asset_risk_platform.audit.application;

import com.example.digital_asset_risk_platform.audit.domain.AuditEventLog;
import com.example.digital_asset_risk_platform.audit.repository.AuditEventLogRepository;
import com.example.digital_asset_risk_platform.common.exception.BusinessException;
import com.example.digital_asset_risk_platform.common.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditEventLogService {

    private final AuditEventLogRepository auditEventLogRepository;
    private final ObjectMapper objectMapper;

    public void saveIfNotExists(String eventId, String eventType, String topicName, String messageKey, Object payload) {
        if (auditEventLogRepository.existsByEventId(eventId)) {
            return;
        }

        AuditEventLog log = new AuditEventLog(eventId, eventType, topicName, messageKey, toJson(payload));

        auditEventLogRepository.save(log);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.EVENT_PAYLOAD_CONVERT_FAILED, "이벤트 payload JSON 변환에 실패했습니다.", e);
        }
    }
}
