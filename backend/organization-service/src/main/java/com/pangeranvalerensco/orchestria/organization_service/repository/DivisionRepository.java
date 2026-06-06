package com.pangeranvalerensco.orchestria.organization_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pangeranvalerensco.orchestria.organization_service.entity.Division;

public interface DivisionRepository extends JpaRepository<Division, Long> {
    Optional<Division> findByCode(String code);
    Optional<Division> findByName(String name);
    List<Division> findByActiveTrueOrderByDisplayOrderAscNameAsc();
    List<Division> findByPublicVisibleTrueAndActiveTrueOrderByDisplayOrderAscNameAsc();
    boolean existsByCode(String code);
    boolean existsByName(String name);
    
}
