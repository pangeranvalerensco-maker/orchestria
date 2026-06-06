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

import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.PositionRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PositionResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.PositionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organization/positions")
@RequiredArgsConstructor
public class PositionController {
    private final PositionService positionService;

    @GetMapping
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getAllPositions(){
        return ResponseEntity.ok(positionService.getAllPositions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<PositionResponse>> getPositionById(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(positionService.getPositionById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<PositionResponse>> createPosition(
            @Valid @RequestBody PositionRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(positionService.createPosition(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<PositionResponse>> updatePosition(
            @PathVariable Long id,
            @Valid @RequestBody PositionRequest request
    ) {
        return ResponseEntity.ok(positionService.updatePosition(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<Void>> deletePosition(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(positionService.deletePosition(id));
    }

    
}
