package com.pangeranvalerensco.orchestria.organization_service.controller;

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

import com.pangeranvalerensco.orchestria.organization_service.payload.request.MemberAssignmentRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.MemberAssignmentResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.MemberAssignmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organization/member-assignments")
@RequiredArgsConstructor
public class MemberAssignmentController {
    
    private final MemberAssignmentService assignmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<List<MemberAssignmentResponse>>> getAllAssignments(){
        return ResponseEntity.ok(assignmentService.getAllAssignment());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<MemberAssignmentResponse>> getAssignmentById(
        @PathVariable Long id
    ){
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }
    
    @GetMapping("/period/{periodId}")
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<List<MemberAssignmentResponse>>> getAssignmentByPeriod(
        @PathVariable Long periodId
    ){
        return ResponseEntity.ok(assignmentService.getAssignmentByPeriod(periodId));
    }
    
    @GetMapping("/period/{periodId}/division/{divisionId}")
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<List<MemberAssignmentResponse>>> getAssignmentByPeriodeAndDivision(
        @PathVariable Long periodId,
        @PathVariable Long divisionId
    ){
        return ResponseEntity.ok(assignmentService.getAssignmentByPeriodAndDivision(periodId, divisionId));
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<MemberAssignmentResponse>> createAssignment(
        @Valid @RequestBody MemberAssignmentRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentService.createAssignment(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<MemberAssignmentResponse>> updateAssignment(
        @PathVariable Long id,
        @Valid @RequestBody MemberAssignmentRequest request
    ){
        return ResponseEntity.ok(assignmentService.updateAssignment(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteAssignment(
        @PathVariable Long id
    ){
        return ResponseEntity.ok(assignmentService.deleteAssignment(id));
    }

}
