package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.payload.request.DivisionTaskEvidenceRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.DivisionTaskEvidenceResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.DivisionTaskEvidenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization/division-task-evidences")
@RequiredArgsConstructor
public class DivisionTaskEvidenceController {

    private final DivisionTaskEvidenceService evidenceService;

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAuthority('division.task.read')")
    public ResponseEntity<ApiResponse<List<DivisionTaskEvidenceResponse>>> getEvidencesByTask(
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(evidenceService.getEvidencesByTask(taskId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('division.task.read')")
    public ResponseEntity<ApiResponse<DivisionTaskEvidenceResponse>> getEvidenceById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(evidenceService.getEvidenceById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('division.task.manage')")
    public ResponseEntity<ApiResponse<DivisionTaskEvidenceResponse>> createEvidence(
            @Valid @RequestBody DivisionTaskEvidenceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evidenceService.createEvidence(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('division.task.manage')")
    public ResponseEntity<ApiResponse<DivisionTaskEvidenceResponse>> updateEvidence(
            @PathVariable Long id,
            @Valid @RequestBody DivisionTaskEvidenceRequest request
    ) {
        return ResponseEntity.ok(evidenceService.updateEvidence(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('division.task.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteEvidence(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(evidenceService.deleteEvidence(id));
    }
}