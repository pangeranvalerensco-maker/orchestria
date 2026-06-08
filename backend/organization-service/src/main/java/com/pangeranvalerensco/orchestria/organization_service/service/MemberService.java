package com.pangeranvalerensco.orchestria.organization_service.service;

import java.util.List;

import com.pangeranvalerensco.orchestria.organization_service.payload.request.MemberRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.MemberResponse;

public interface MemberService {
    
    ApiResponse<List<MemberResponse>> getAllMembers();

    ApiResponse<MemberResponse> getMemberById(Long id);

    ApiResponse<MemberResponse> createMember(MemberRequest request);

    ApiResponse<MemberResponse> updateMember(Long id, MemberRequest request);

    ApiResponse<Void> deleteMember(Long id);
}
