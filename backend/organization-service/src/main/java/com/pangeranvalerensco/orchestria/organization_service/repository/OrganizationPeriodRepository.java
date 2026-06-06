package com.pangeranvalerensco.orchestria.organization_service.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pangeranvalerensco.orchestria.organization_service.entity.OrganizationPeriod;

public interface OrganizationPeriodRepository extends JpaRepository<OrganizationPeriod, Long> {
    
    Optional<OrganizationPeriod> findByName(String name);
    Optional<OrganizationPeriod> findByCurrentPeriodTrue();
    List<OrganizationPeriod> findByPublicVisibleTrueOrderByStartDateDesc();
    List<OrganizationPeriod> findByActiveTrueOrderByStartDateDesc();
    boolean existsByName(String name);
}
