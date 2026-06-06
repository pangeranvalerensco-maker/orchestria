package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.Division;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.MemberAssignment;
import com.pangeranvalerensco.orchestria.organization_service.entity.OrganizationPeriod;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberAssignmentRepository extends JpaRepository<MemberAssignment, Long> {

    List<MemberAssignment> findByMemberAndStatusAndActiveTrue(Member member, AssignmentStatus status);

    List<MemberAssignment> findByPeriodAndStatusAndActiveTrueOrderByPositionLevelOrderAscMemberFullNameAsc(
            OrganizationPeriod period,
            AssignmentStatus status
    );

    List<MemberAssignment> findByPeriodAndDivisionAndStatusAndActiveTrueOrderByPositionLevelOrderAscMemberFullNameAsc(
            OrganizationPeriod period,
            Division division,
            AssignmentStatus status
    );
}