package com.pangeranvalerensco.orchestria.auth_service.payload.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SessionDemoUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private List<String> roles;
}
