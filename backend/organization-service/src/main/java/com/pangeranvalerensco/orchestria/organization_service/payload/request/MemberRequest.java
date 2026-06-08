package com.pangeranvalerensco.orchestria.organization_service.payload.request;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.MemberStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MemberRequest {
    
    private Long authUserId;

    @NotBlank(message = "Nama lengkap Wajib diisi")
    @Size(max = 150, message = "Nama lengkap maksimal 150 karakter")
    private String fullName;

    @NotBlank(message = "Email Wajib diisi")
    @Email(message = "Format email tidak valid")
    @Size(max = 150, message = "Email maksimal 150 karakter")
    private String email;

    @Size(max = 50, message = "NIM maksimal 50 karakter")
    private String studentNumber;

    @Size(max = 30, message = "Nomor Telepon maksimal 30 karakter")
    private String phoneNumber;

    @Size(max = 100, message = "Angkatan maksimal 100 karakter")
    private String cohort;

    @Size(max = 500, message = "URL foto profil maksimal 500 karakter")
    private String profilePhotoUrl;

    @Size(max = 100, message = "Jurusan Maksimal 100 karakter")
    private String major;

    @Size(max = 100, message = "Kelas Maksimal 100 karakter")
    private String campusClass;

    private Boolean publicVisible = true;

    private Integer displayOrder = 99;

    private MemberStatus status = MemberStatus.ACTIVE;
}
