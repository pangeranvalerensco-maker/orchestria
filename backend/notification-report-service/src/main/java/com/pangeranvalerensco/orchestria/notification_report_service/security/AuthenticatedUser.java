package com.pangeranvalerensco.orchestria.notification_report_service.security;

import java.security.Principal;
import java.util.List;

public record AuthenticatedUser(
        Long userId,
        String email,
        String fullName,
        List<String> roles
) implements Principal {

    @Override
    public String getName() {
        return email;
    }
}
