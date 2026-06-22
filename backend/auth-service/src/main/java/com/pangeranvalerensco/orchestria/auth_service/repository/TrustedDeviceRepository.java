package com.pangeranvalerensco.orchestria.auth_service.repository;

import com.pangeranvalerensco.orchestria.auth_service.entity.TrustedDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, String> {

    Optional<TrustedDevice> findByUserIdAndTokenHash(Long userId, String tokenHash);
    
    Optional<TrustedDevice> findByTokenHash(String tokenHash);

    List<TrustedDevice> findAllByUserIdAndRevokedAtIsNull(Long userId);

    @Modifying
    @Query("UPDATE TrustedDevice t SET t.revokedAt = :now WHERE t.userId = :userId AND t.revokedAt IS NULL")
    void revokeAllByUserId(Long userId, LocalDateTime now);
}
