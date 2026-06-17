package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.OrganizationPeriodResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PublicMemberResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PublicOrganizationStructureResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.PublicOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization/public")
@RequiredArgsConstructor
public class PublicOrganizationController {

    private final PublicOrganizationService publicOrganizationService;

    @GetMapping("/periods")
    public ResponseEntity<ApiResponse<List<OrganizationPeriodResponse>>> getPublicPeriods() {
        return ResponseEntity.ok(publicOrganizationService.getPublicPeriods());
    }

    @GetMapping("/periods/current")
    public ResponseEntity<ApiResponse<OrganizationPeriodResponse>> getCurrentPeriod() {
        return ResponseEntity.ok(publicOrganizationService.getCurrentPeriod());
    }

    @GetMapping("/members")
    public ResponseEntity<ApiResponse<List<PublicMemberResponse>>> getPublicMembersByCohort(
            @RequestParam String cohort
    ) {
        return ResponseEntity.ok(publicOrganizationService.getPublicMembersByCohort(cohort));
    }

    @GetMapping("/structure/current")
    public ResponseEntity<ApiResponse<PublicOrganizationStructureResponse>> getCurrentStructure() {
        return ResponseEntity.ok(publicOrganizationService.getCurrentStructure());
    }

    @GetMapping("/structure/period/{periodId}")
    public ResponseEntity<ApiResponse<PublicOrganizationStructureResponse>> getStructureByPeriod(
            @PathVariable Long periodId
    ) {
        return ResponseEntity.ok(publicOrganizationService.getStructureByPeriod(periodId));
    }
}