package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentType;
import com.pangeranvalerensco.orchestria.organization_service.entity.PublicationStatus;
import com.pangeranvalerensco.orchestria.organization_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PublicContentResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.PublicContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization/public/content")
@RequiredArgsConstructor
public class PublicContentController {

    private final PublicContentService publicContentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PublicContentResponse>>> getPublishedContents(
            @RequestParam(required = false) PublicContentType type,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.<List<PublicContentResponse>>builder().success(true).message("Berhasil mengambil konten publik").data(publicContentService.getPublishedContents(type, category)).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PublicContentResponse>> getPublishedContent(@PathVariable String id) {
        PublicContentResponse response = publicContentService.getContent(id);
        if (response.getPublicationStatus() != PublicationStatus.PUBLISHED || !response.isActive()) {
            throw new BadRequestException("Konten tidak ditemukan atau tidak tersedia untuk publik");
        }
        return ResponseEntity.ok(ApiResponse.<PublicContentResponse>builder().success(true).message("Berhasil mengambil detail konten").data(response).build());
    }

    // Maintain old endpoint for compatibility
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<PublicContentResponse>>> getContentsByType(@PathVariable PublicContentType type) {
        return ResponseEntity.ok(ApiResponse.<List<PublicContentResponse>>builder().success(true).message("Berhasil mengambil konten publik berdasarkan tipe").data(publicContentService.getPublishedContents(type, null)).build());
    }
}
