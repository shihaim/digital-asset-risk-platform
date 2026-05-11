package com.example.digital_asset_risk_platform.common.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        int status,
        List<FieldError> errors,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getStatus(),
                List.of(),
                LocalDateTime.now()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(
                errorCode.getCode(),
                message,
                errorCode.getStatus(),
                List.of(),
                LocalDateTime.now()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                errorCode.getStatus(),
                errors,
                LocalDateTime.now()
        );
    }

    public record FieldError(
            String field,
            String rejectedValue,
            String reason
    ) {
        public static FieldError of(
                String field,
                Object rejectedValue,
                String reason
        ) {
            return new FieldError(
                    field,
                    rejectedValue == null ? null : String.valueOf(rejectedValue),
                    reason
            );
        }
    }
}
