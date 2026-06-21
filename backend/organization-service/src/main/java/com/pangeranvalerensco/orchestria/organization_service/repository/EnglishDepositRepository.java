package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.EnglishDeposit;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EnglishDepositStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnglishDepositRepository extends JpaRepository<EnglishDeposit, String> {
    List<EnglishDeposit> findByActiveTrueOrderBySubmittedAtDesc();
    List<EnglishDeposit> findByMemberIdAndActiveTrueOrderBySubmittedAtDesc(Long memberId);
    List<EnglishDeposit> findByActivityIdAndActiveTrue(String activityId);
    Optional<EnglishDeposit> findByIdAndActiveTrue(String id);
    Optional<EnglishDeposit> findByActivityIdAndMemberIdAndActiveTrue(String activityId, Long memberId);
    boolean existsByActivityIdAndMemberIdAndActiveTrue(String activityId, Long memberId);
    long countByActiveTrue();
    long countByActiveTrueAndStatus(EnglishDepositStatus status);
}
