package com.pangeranvalerensco.orchestria.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trusted_devices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrustedDevice {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String tokenHash;

    @Column(length = 255)
    private String deviceName;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 45)
    private String lastIpAddress;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime lastUsedAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;

}
