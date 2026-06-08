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

import com.pangeranvalerensco.orchestria.organization_service.payload.request.MemberRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.MemberResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organization/members")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;

    @GetMapping
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getAllMembers(){
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.read')")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(
                @PathVariable Long id
    ){
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<MemberResponse>> createMember(
                @Valid @RequestBody MemberRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                    .body(memberService.createMember(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
                @PathVariable Long id,
                @Valid @RequestBody MemberRequest request
    ){
        return ResponseEntity.ok(memberService.updateMember(id, request));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('organization.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteMember(
                @PathVariable Long id
    ){
        return ResponseEntity.ok(memberService.deleteMember(id));
    }
}
