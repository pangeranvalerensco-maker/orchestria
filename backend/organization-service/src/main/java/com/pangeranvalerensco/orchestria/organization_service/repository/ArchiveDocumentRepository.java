package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.ArchiveDocument;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArchiveDocumentRepository extends JpaRepository<ArchiveDocument, Long> {

    List<ArchiveDocument> findByDeletedFalseOrderByUploadedAtDesc();

    List<ArchiveDocument> findByDeletedFalseAndCategoryOrderByUploadedAtDesc(
            DocumentCategory category
    );

    @Query("""
            SELECT d FROM ArchiveDocument d
            WHERE d.deleted = false
              AND (
                   LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY d.uploadedAt DESC
            """)
    List<ArchiveDocument> searchActiveDocuments(
            @Param("keyword") String keyword
    );

    @Query("""
            SELECT d FROM ArchiveDocument d
            WHERE d.deleted = false
              AND d.category = :category
              AND (
                   LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(d.originalFileName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY d.uploadedAt DESC
            """)
    List<ArchiveDocument> searchActiveDocumentsByCategory(
            @Param("keyword") String keyword,
            @Param("category") DocumentCategory category
    );

    Optional<ArchiveDocument> findByIdAndDeletedFalse(Long id);
}
