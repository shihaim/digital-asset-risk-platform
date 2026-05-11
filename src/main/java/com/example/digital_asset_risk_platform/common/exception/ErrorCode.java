package com.example.digital_asset_risk_platform.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "요청 값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "서버 내부 오류가 발생했습니다."),

    // Withdrawal
    WITHDRAWAL_NOT_FOUND(HttpStatus.NOT_FOUND, "WITHDRAWAL_001", "출금 요청을 찾을 수 없습니다."),
    INVALID_WITHDRAWAL_STATUS(HttpStatus.BAD_REQUEST, "WITHDRAWAL_002", "현재 출금 상태에서는 요청한 처리를 수행할 수 없습니다."),
    WITHDRAWAL_ALREADY_EVALUATED(HttpStatus.CONFLICT, "WITHDRAWAL_003", "이미 FDS 평가가 완료된 출금 요청입니다."),

    // Risk Evaluation
    RISK_EVALUATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RISK_EVALUATION_001", "위험 평가 결과를 찾을 수 없습니다."),
    DUPLICATE_RISK_EVALUATION(HttpStatus.CONFLICT, "RISK_EVALUATION_002", "이미 동일 대상에 대한 위험 평가가 존재합니다."),
    UNSUPPORTED_RISK_DECISION(HttpStatus.BAD_REQUEST, "RISK_EVALUATION_003", "지원하지 않는 위험 판단 결과입니다."),

    // Risk Case
    RISK_CASE_NOT_FOUND(HttpStatus.NOT_FOUND, "RISK_CASE_001", "RiskCase를 찾을 수 없습니다."),
    INVALID_RISK_CASE_STATUS(HttpStatus.BAD_REQUEST, "RISK_CASE_002", "현재 Case 상태에서는 요청한 처리를 수행할 수 없습니다."),

    // Wallet Risk
    WALLET_RISK_NOT_FOUND(HttpStatus.NOT_FOUND, "WALLET_RISK_001", "지갑 위험도 정보를 찾을 수 없습니다."),
    DUPLICATE_WALLET_RISK(HttpStatus.CONFLICT, "WALLET_RISK_002", "이미 등록된 지갑 위험도 정보입니다."),

    // Event / Outbox
    UNSUPPORTED_OUTBOX_EVENT_TYPE(HttpStatus.BAD_REQUEST, "OUTBOX_001", "지원하지 않는 Outbox 이벤트 타입입니다."),
    OUTBOX_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "OUTBOX_002", "Outbox 이벤트를 찾을 수 없습니다."),
    OUTBOX_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OUTBOX_003", "Outbox 이벤트 발행에 실패했습니다."),

    // Consumer
    DUPLICATE_EVENT(HttpStatus.CONFLICT, "EVENT_001", "이미 처리된 이벤트입니다."),
    EVENT_PAYLOAD_CONVERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EVENT_002", "이벤트 payload 변환에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getStatus() {
        return httpStatus.value();
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
