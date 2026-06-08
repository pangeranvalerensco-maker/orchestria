package com.pangeranvalerensco.orchestria.organization_service.controller;

import java.net.ResponseCache;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pangeranvalerensco.orchestria.organization_service.payload.request.OrganizationPeriodRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.OrganizationPeriodResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.OrganizationPeriodService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/organization/periods")
@RequiredArgsConstructor
public class OrganizationPeriodController {

    private final OrganizationPeriodService periodService;

    @GetMapping
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<List<OrganizationPeriodResponse>>> getAllPeriods() {
        return ResponseEntity.ok(periodService.getAllPeriods());
    }

    @GetMapping("/current")
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<OrganizationPeriodResponse>> getCurrentPeriod() {
        return ResponseEntity.ok(periodService.getCurrentPeriod());
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<OrganizationPeriodResponse>> getPeriodById(
            @PathVariable Long id) {
        return ResponseEntity.ok(periodService.getPeriodById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<OrganizationPeriodResponse>> createPeriod(
            @Valid @RequestBody OrganizationPeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(periodService.createPeriode(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<OrganizationPeriodResponse>> updatePeriod(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationPeriodRequest request) {
        return ResponseEntity.ok(periodService.updatePeriod(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<Void>> deletePeriod(
            @PathVariable Long id) {
        return ResponseEntity.ok(periodService.deletePeriode(id));
    }
}
