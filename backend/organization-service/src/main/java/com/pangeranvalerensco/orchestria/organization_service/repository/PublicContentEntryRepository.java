package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentEntry;
import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentType;
import com.pangeranvalerensco.orchestria.organization_service.entity.PublicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicContentEntryRepository extends JpaRepository<PublicContentEntry, String> {
    List<PublicContentEntry> findByActiveTrueAndPublicationStatusOrderByDisplayOrderAscPublishedAtDesc(PublicationStatus status);
    
    List<PublicContentEntry> findByContentTypeAndActiveTrueAndPublicationStatusOrderByDisplayOrderAscPublishedAtDesc(PublicContentType type, PublicationStatus status);

    List<PublicContentEntry> findByContentTypeAndCategoryAndActiveTrueAndPublicationStatusOrderByDisplayOrderAscPublishedAtDesc(PublicContentType type, String category, PublicationStatus status);

    long countByContentTypeAndPublicationStatusAndActiveTrue(PublicContentType type, PublicationStatus status);
}
