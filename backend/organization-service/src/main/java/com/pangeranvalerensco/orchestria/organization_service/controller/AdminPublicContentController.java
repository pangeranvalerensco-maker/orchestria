package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentType;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.PublicContentRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PublicContentResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.PublicContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization/public-content")
@RequiredArgsConstructor
public class AdminPublicContentController {

    private final PublicContentService publicContentService;

    @GetMapping
    @PreAuthorize("hasAuthority('public.content.read')")
    public ResponseEntity<ApiResponse<List<PublicContentResponse>>> getAllContents() {
        return ResponseEntity.ok(ApiResponse.<List<PublicContentResponse>>builder().success(true).message("Berhasil mengambil konten publik").data(publicContentService.getAllContents()).build());
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAuthority('public.content.read')")
    public ResponseEntity<ApiResponse<List<PublicContentResponse>>> getContentsByType(@PathVariable PublicContentType type) {
        return ResponseEntity.ok(ApiResponse.<List<PublicContentResponse>>builder().success(true).message("Berhasil mengambil konten publik berdasarkan tipe").data(publicContentService.getContentsByType(type)).build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('public.content.manage', 'public.organization.manage', 'public.activity.manage', 'public.media.manage')")
    public ResponseEntity<ApiResponse<PublicContentResponse>> createContent(
            @Valid @RequestBody PublicContentRequest request) {
        
        checkPermission(request.getContentType());
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.<PublicContentResponse>builder().success(true).message("Konten berhasil dibuat").data(publicContentService.createContent(request, userEmail)).build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('public.content.manage', 'public.organization.manage', 'public.activity.manage', 'public.media.manage')")
    public ResponseEntity<ApiResponse<PublicContentResponse>> updateContent(
            @PathVariable String id,
            @Valid @RequestBody PublicContentRequest request) {
        
        checkPermission(request.getContentType());
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.<PublicContentResponse>builder().success(true).message("Konten berhasil diperbarui").data(publicContentService.updateContent(id, request, userEmail)).build());
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyAuthority('public.content.manage', 'public.organization.manage', 'public.activity.manage', 'public.media.manage')")
    public ResponseEntity<ApiResponse<PublicContentResponse>> publishContent(
            @PathVariable String id,
            @RequestParam PublicContentType type) {
        checkPermission(type);
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.<PublicContentResponse>builder().success(true).message("Konten berhasil dipublikasikan").data(publicContentService.publishContent(id, userEmail)).build());
    }

    @PutMapping("/{id}/archive")
    @PreAuthorize("hasAnyAuthority('public.content.manage', 'public.organization.manage', 'public.activity.manage', 'public.media.manage')")
    public ResponseEntity<ApiResponse<PublicContentResponse>> archiveContent(
            @PathVariable String id,
            @RequestParam PublicContentType type) {
        checkPermission(type);
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.<PublicContentResponse>builder().success(true).message("Konten berhasil diarsipkan").data(publicContentService.archiveContent(id, userEmail)).build());
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAnyAuthority('public.content.manage', 'public.organization.manage', 'public.activity.manage', 'public.media.manage')")
    public ResponseEntity<ApiResponse<PublicContentResponse>> restoreContent(
            @PathVariable String id,
            @RequestParam PublicContentType type) {
        checkPermission(type);
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.<PublicContentResponse>builder().success(true).message("Konten berhasil dikembalikan ke DRAFT").data(publicContentService.restoreDraftContent(id, userEmail)).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('public.content.manage', 'public.organization.manage', 'public.activity.manage', 'public.media.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @PathVariable String id,
            @RequestParam PublicContentType type) {
        checkPermission(type);
        publicContentService.deleteContent(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Konten berhasil dihapus").data(null).build());
    }
    
    private void checkPermission(PublicContentType type) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = authentication.getAuthorities();
        
        boolean hasPermission = false;
        switch (type) {
            case HERO:
            case ABOUT:
            case VISION:
            case MISSION:
                hasPermission = authorities.stream().anyMatch(a -> a.getAuthority().equals("public.organization.manage"));
                break;
            case PROGRAM:
            case FACILITY:
            case TESTIMONIAL:
                hasPermission = authorities.stream().anyMatch(a -> a.getAuthority().equals("public.content.manage"));
                break;
            case ACTIVITY:
                hasPermission = authorities.stream().anyMatch(a -> a.getAuthority().equals("public.activity.manage"));
                break;
            case MEDIA:
                hasPermission = authorities.stream().anyMatch(a -> a.getAuthority().equals("public.media.manage"));
                break;
        }
        
        if (!hasPermission) {
            throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki akses untuk mengelola tipe konten ini.");
        }
    }
}
