package com.example.digital_asset_risk_platform.risk.profile.repository;

import com.example.digital_asset_risk_platform.risk.profile.domain.UserRiskProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRiskProfileRepository extends JpaRepository<UserRiskProfile, Long> {

    Optional<UserRiskProfile> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select p
        from UserRiskProfile p
        where p.userId = :userId
    """)
    Optional<UserRiskProfile> findByUserIdForUpdate(@Param("userId") Long userId);
}
