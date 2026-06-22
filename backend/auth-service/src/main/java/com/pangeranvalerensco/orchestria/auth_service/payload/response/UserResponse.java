package com.pangeranvalerensco.orchestria.auth_service.payload.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private Boolean active;
    private Set<String> roles;
    private Set<String> permissions;
    private Boolean twoFactorEnabled;
    private Boolean twoFactorRequired;

    /**
     * Login operasional Orchestria saat ini tidak mewajibkan OTP.
     *
     * AuthServiceImpl menggunakan getter ini untuk menentukan apakah login harus
     * dilanjutkan ke challenge OTP. Mengembalikan false membuat login berhasil
     * langsung setelah email dan password tervalidasi, tanpa menghapus workflow
     * OTP lain seperti lupa password dan konfirmasi pengaturan keamanan.
     */
    public Boolean getTwoFactorRequired() {
        return false;
    }
}
