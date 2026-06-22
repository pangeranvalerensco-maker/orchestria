package com.pangeranvalerensco.orchestria.auth_service.repository;

import com.pangeranvalerensco.orchestria.auth_service.entity.PasswordResetGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PasswordResetGrantRepository extends JpaRepository<PasswordResetGrant, String> {

    @Modifying
    @Query("UPDATE PasswordResetGrant p SET p.usedAt = :now WHERE p.userId = :userId AND p.usedAt IS NULL")
    void invalidateAllActiveGrants(Long userId, LocalDateTime now);
}
