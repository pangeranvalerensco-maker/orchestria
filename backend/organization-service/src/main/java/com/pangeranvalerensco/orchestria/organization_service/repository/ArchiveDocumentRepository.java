package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.ArchiveDocument;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArchiveDocumentRepository extends JpaRepository<ArchiveDocument, Long> {

    @Query("""
            SELECT d FROM ArchiveDocument d
            WHERE d.deleted = false
              AND (:keyword IS NULL
                   OR LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:category IS NULL OR d.category = :category)
            ORDER BY d.uploadedAt DESC
            """)
    List<ArchiveDocument> findActiveDocuments(
            @Param("keyword") String keyword,
            @Param("category") DocumentCategory category
    );

    Optional<ArchiveDocument> findByIdAndDeletedFalse(Long id);
}
