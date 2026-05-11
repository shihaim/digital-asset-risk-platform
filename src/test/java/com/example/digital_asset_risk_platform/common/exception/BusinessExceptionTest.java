package com.example.digital_asset_risk_platform.common.exception;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BusinessExceptionTest {

    @Test
    @DisplayName("BusinessException은 ErrorCode를 가진다")
    void case1() {
        //when
        BusinessException exception = new BusinessException(ErrorCode.WITHDRAWAL_NOT_FOUND);

        //then
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WITHDRAWAL_NOT_FOUND);
        Assertions.assertThat(exception.getMessage()).isEqualTo("출금 요청을 찾을 수 없습니다.");
        Assertions.assertThat(exception.getDetailMessage()).isEqualTo("출금 요청을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("BusinessException은 상세 메시지를 가질 수 있다")
    void case2() {
        // when
        BusinessException exception = new BusinessException(
                ErrorCode.WITHDRAWAL_NOT_FOUND,
                "출금 요청을 찾을 수 없습니다. withdrawalId=1"
        );

        // then
        Assertions.assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WITHDRAWAL_NOT_FOUND);
        Assertions.assertThat(exception.getDetailMessage()).isEqualTo("출금 요청을 찾을 수 없습니다. withdrawalId=1");
    }
}