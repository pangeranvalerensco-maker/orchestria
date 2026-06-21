package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentType;
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

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<PublicContentResponse>>> getContentsByType(@PathVariable PublicContentType type) {
        return ResponseEntity.ok(ApiResponse.<List<PublicContentResponse>>builder().success(true).message("Berhasil mengambil konten publik berdasarkan tipe").data(publicContentService.getPublishedContentsByType(type)).build());
    }

    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<List<PublicContentResponse>>> getActivitiesByCategory(@RequestParam String category) {
        return ResponseEntity.ok(ApiResponse.<List<PublicContentResponse>>builder().success(true).message("Berhasil mengambil aktivitas publik berdasarkan kategori").data(publicContentService.getPublishedActivitiesByCategory(category)).build());
    }
}
