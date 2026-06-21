package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.CleanlinessSchedule;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CleanlinessScheduleRepository extends JpaRepository<CleanlinessSchedule, String> {
    List<CleanlinessSchedule> findByActiveTrueOrderByDutyDateDescStartTimeDesc();
    List<CleanlinessSchedule> findByStatusAndActiveTrueOrderByDutyDateDescStartTimeDesc(ScheduleStatus status);
    Optional<CleanlinessSchedule> findByIdAndActiveTrue(String id);
}
