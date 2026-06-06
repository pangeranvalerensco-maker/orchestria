package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByAuthUserId(Long authUserId);

    List<Member> findByCohortAndPublicVisibleTrueAndActiveTrueOrderByDisplayOrderAscFullNameAsc(String cohort);

    List<Member> findByStatusAndActiveTrueOrderByFullNameAsc(MemberStatus status);

    boolean existsByEmail(String email);

    boolean existsByStudentNumber(String studentNumber);
}