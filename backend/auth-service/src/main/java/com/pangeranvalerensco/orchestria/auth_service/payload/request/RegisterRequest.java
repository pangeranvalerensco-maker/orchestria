package com.pangeranvalerensco.orchestria.auth_service.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "Nama Lengkap Wajib Diisi")
    @Size(max = 150, message = "Nama Lengkap Maksimal 150 Karakter")
    private String fullName;

    @NotBlank(message = "Email Wajib Diisi")
    @Email(message = "Format Email Tidak Valid")
    @Size(max = 150, message = "Email Maksimal 150 Karakter")
    private String email;

    @NotBlank(message = "Password Wajib Diisi")
    @Size(min = 6, max = 100, message = "Password Minimal 6 Karakter dan Maksimal 100 Karakter")
    private String password;
}
