package com.pangeranvalerensco.orchestria.organization_service.entity;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.PointRecordType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cleanliness_point_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CleanlinessPointRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private String id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = true)
    private String scheduleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointRecordType type;

    @Column(nullable = false)
    private int pointValue;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private String recordedByEmail;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(nullable = false)
    private boolean active = true;
}
