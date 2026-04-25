package com.example.digital_asset_risk_platform.wallet.repository;

import com.example.digital_asset_risk_platform.wallet.domain.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
    boolean existsByUserIdAndChainTypeAndToAddressAndIdNot(Long userId, String chainType, String toAddress, Long id);

    long countByUserIdAndRequestedAt(Long userId, LocalDateTime requestedAt);

    @Query("""
        select coalesce(avg(w.amount), 0)
          from WithdrawalRequest w
         where w.userId = :userId
    """)
    BigDecimal averageAmountByUserId(Long userId);
}
