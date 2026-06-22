package com.pangeranvalerensco.orchestria.auth_service.repository;

import com.pangeranvalerensco.orchestria.auth_service.entity.OtpChallenge;
import com.pangeranvalerensco.orchestria.auth_service.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, String> {

    @Modifying
    @Query("UPDATE OtpChallenge o SET o.consumedAt = :now WHERE o.userId = :userId AND o.purpose = :purpose AND o.consumedAt IS NULL")
    void invalidateActiveChallenges(Long userId, OtpPurpose purpose, LocalDateTime now);

    @Modifying
    @Query("UPDATE OtpChallenge o SET o.consumedAt = :now WHERE o.userId = :userId AND o.consumedAt IS NULL")
    void invalidateAllActiveChallenges(Long userId, LocalDateTime now);
}
