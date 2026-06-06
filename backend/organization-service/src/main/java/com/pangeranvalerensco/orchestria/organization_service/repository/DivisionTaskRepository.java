package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.Division;
import com.pangeranvalerensco.orchestria.organization_service.entity.DivisionTask;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DivisionTaskRepository extends JpaRepository<DivisionTask, Long> {

    List<DivisionTask> findByDivisionAndActiveTrueOrderByDueDateAscCreatedAtDesc(Division division);

    List<DivisionTask> findByAssignedMemberAndActiveTrueOrderByDueDateAscCreatedAtDesc(Member assignedMember);

    List<DivisionTask> findByStatusAndActiveTrueOrderByDueDateAscCreatedAtDesc(TaskStatus status);
}