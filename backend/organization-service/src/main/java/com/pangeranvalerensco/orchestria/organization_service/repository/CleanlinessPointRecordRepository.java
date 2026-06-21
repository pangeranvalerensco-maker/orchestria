package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.CleanlinessPointRecord;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.PointRecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CleanlinessPointRecordRepository extends JpaRepository<CleanlinessPointRecord, String> {
    List<CleanlinessPointRecord> findByMemberIdAndActiveTrueOrderByRecordedAtDesc(Long memberId);
    List<CleanlinessPointRecord> findByActiveTrueOrderByRecordedAtDesc();

    @Query("SELECT COALESCE(SUM(p.pointValue), 0) FROM CleanlinessPointRecord p WHERE p.memberId = :memberId AND p.type = :type AND p.active = true")
    int sumPointsByMemberIdAndType(@Param("memberId") Long memberId, @Param("type") PointRecordType type);

    @Query("SELECT COALESCE(SUM(p.pointValue), 0) FROM CleanlinessPointRecord p WHERE p.type = :type AND p.active = true")
    int sumTotalPointsByType(@Param("type") PointRecordType type);
}
