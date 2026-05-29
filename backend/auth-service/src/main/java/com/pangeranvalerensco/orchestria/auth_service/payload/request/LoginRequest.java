package com.pangeranvalerensco.orchestria.auth_service.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "Email Wajib Diisi")
    @Email(message = "Format Email Tidak Valid")
    private String email;

    @NotBlank(message = "Password Wajib Diisi")
    private String password;
}
