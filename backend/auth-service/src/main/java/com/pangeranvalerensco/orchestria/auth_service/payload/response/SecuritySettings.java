package com.pangeranvalerensco.orchestria.auth_service.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SecuritySettings {
    private Boolean twoFactorEnabled;
    private Boolean twoFactorRequired;
    private Boolean mandatoryByRole;
    private Integer trustedDeviceCount;
}
