package com.pangeranvalerensco.orchestria.organization_service.service;

import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentType;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.PublicContentRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PublicContentResponse;

import java.util.List;

public interface PublicContentService {
    List<PublicContentResponse> getAllContents();
    List<PublicContentResponse> getContentsByType(PublicContentType type);
    List<PublicContentResponse> getPublishedContentsByType(PublicContentType type);
    List<PublicContentResponse> getPublishedActivitiesByCategory(String category);
    
    PublicContentResponse createContent(PublicContentRequest request, String currentUserEmail);
    PublicContentResponse updateContent(String id, PublicContentRequest request, String currentUserEmail);
    
    PublicContentResponse publishContent(String id, String currentUserEmail);
    PublicContentResponse archiveContent(String id, String currentUserEmail);
    PublicContentResponse restoreDraftContent(String id, String currentUserEmail);
    
    void deleteContent(String id);
}
