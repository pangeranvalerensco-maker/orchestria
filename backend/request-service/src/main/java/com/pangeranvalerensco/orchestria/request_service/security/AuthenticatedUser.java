package com.pangeranvalerensco.orchestria.request_service.security;

import java.security.Principal;

public record AuthenticatedUser(
        Long userId,
        String email,
        String fullName
) implements Principal {

    @Override
    public String getName() {
        return email;
    }
}