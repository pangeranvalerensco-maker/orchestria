package com.pangeranvalerensco.orchestria.organization_service.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pangeranvalerensco.orchestria.organization_service.entity.OrganizationPeriod;

public interface OrganizationRepository extends JpaRepository<OrganizationPeriod, Long> {
    
    Optional<OrganizationPeriod> findByName(String name);
    Optional<OrganizationPeriod> findByCurrentPeriodTrue();
    List<OrganizationPeriod> findByPublicVisibleTrueOrderByStartDateDesc();
    boolean existsByName(String name);
}
