package com.pangeranvalerensco.orchestria.organization_service.service;

import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentType;
import com.pangeranvalerensco.orchestria.organization_service.entity.PublicationStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.PublicContentRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PublicContentResponse;

import java.util.List;

public interface PublicContentService {
    List<PublicContentResponse> getAllContents(PublicContentType type, PublicationStatus status, Boolean active);
    PublicContentResponse getContent(String id);
    PublicContentType getContentType(String id);
    
    List<PublicContentResponse> getPublishedContents(PublicContentType type, String category);
    
    PublicContentResponse createContent(PublicContentRequest request, String currentUserEmail);
    PublicContentResponse updateContent(String id, PublicContentRequest request, String currentUserEmail);
    
    PublicContentResponse publishContent(String id, String currentUserEmail);
    PublicContentResponse archiveContent(String id, String currentUserEmail);
    PublicContentResponse restoreDraftContent(String id, String currentUserEmail);
    
    void deleteContent(String id, String currentUserEmail);
}
