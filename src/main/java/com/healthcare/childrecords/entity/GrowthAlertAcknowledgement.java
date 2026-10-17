package com.healthcare.childrecords.entity;

import com.healthcare.childrecords.util.EncryptionConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "growth_alert_acknowledgements")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrowthAlertAcknowledgement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "alert_key", nullable = false, length = 64)
    private String alertKey;

    @Column(name = "acknowledged_at", nullable = false)
    private LocalDateTime acknowledgedAt;

    @Convert(converter = EncryptionConverter.class)
    @Column(columnDefinition = "TEXT")
    private String note;
}
