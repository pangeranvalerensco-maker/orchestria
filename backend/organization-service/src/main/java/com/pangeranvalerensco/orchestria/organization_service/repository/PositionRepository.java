package com.pangeranvalerensco.orchestria.organization_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pangeranvalerensco.orchestria.organization_service.entity.Position;

public interface PositionRepository extends JpaRepository<Position, Long> {
    Optional<Position> findByCode(String code);
    Optional<Position> findByName(String name);
    List<Position> findByActiveTrueOrderByLevelOrderAscNameAsc();
    List<Position> findByPublicVisibleTrueAndActiveTrueOrderByLevelOrderAscNameAsc();
    boolean existsByCode(String code);
    boolean existsByName(String name);
    
}
