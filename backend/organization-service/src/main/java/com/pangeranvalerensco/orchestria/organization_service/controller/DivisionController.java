package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.payload.request.DivisionRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.DivisionResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.DivisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization/divisions")
@RequiredArgsConstructor
public class DivisionController {

    private final DivisionService divisionService;

    @GetMapping
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<List<DivisionResponse>>> getAllDivisions() {
        return ResponseEntity.ok(divisionService.getAllDivisions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<DivisionResponse>> getDivisionById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(divisionService.getDivisionById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<DivisionResponse>> createDivision(
            @Valid @RequestBody DivisionRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                    .body(divisionService.createDivision(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<DivisionResponse>> updateDivision(
            @PathVariable Long id,
            @Valid @RequestBody DivisionRequest request
    ){
        return ResponseEntity.ok(divisionService.updateDivision(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteDivision(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(divisionService.deleteDivision(id));
    }
    
}
