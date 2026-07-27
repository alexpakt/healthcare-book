package com.healthcare.childrecords.entity;

import com.healthcare.childrecords.util.EncryptionConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "allergies")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @Convert(converter = EncryptionConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String allergen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllergySeverity severity;

    @Convert(converter = EncryptionConverter.class)
    @Column(columnDefinition = "TEXT")
    private String reaction;

    @Column(name = "diagnosed_date")
    private LocalDate diagnosedDate;

    @Convert(converter = EncryptionConverter.class)
    @Column(columnDefinition = "TEXT")
    private String notes;
}
