package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.DivisionTask;
import com.pangeranvalerensco.orchestria.organization_service.entity.DivisionTaskEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DivisionTaskEvidenceRepository extends JpaRepository<DivisionTaskEvidence, Long> {

    List<DivisionTaskEvidence> findByTaskAndActiveTrueOrderByCreatedAtDesc(DivisionTask task);
}