package com.pangeranvalerensco.orchestria.organization_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "positions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Contoh:
     * KETUA_PUB, SEKRETARIS, BENDAHARA, KETUA_DIVISI, KOORDINATOR, ANGGOTA
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /**
     * Semakin kecil nilainya, semakin tinggi level jabatannya.
     * Contoh:
     * 1 = Pembina
     * 2 = Ketua PUB
     * 3 = Sekretaris/Bendahara
     * 4 = Ketua Divisi
     * 5 = Anggota
     */
    @Builder.Default
    @Column(nullable = false)
    private Integer levelOrder = 99;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}