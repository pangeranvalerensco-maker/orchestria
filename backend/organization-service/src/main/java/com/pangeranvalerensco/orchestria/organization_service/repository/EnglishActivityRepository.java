package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.EnglishActivity;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EnglishActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnglishActivityRepository extends JpaRepository<EnglishActivity, String> {
    List<EnglishActivity> findByActiveTrueOrderByActivityDateDescStartTimeDesc();
    List<EnglishActivity> findByActiveTrueAndStatusOrderByActivityDateDescStartTimeDesc(EnglishActivityStatus status);
    Optional<EnglishActivity> findByIdAndActiveTrue(String id);
    long countByActiveTrue();
    long countByActiveTrueAndStatus(EnglishActivityStatus status);
}
