package com.healthcare.childrecords.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vaccine_combinations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccineCombination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combination_vaccine_id", nullable = false)
    private Vaccine combinationVaccine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_vaccine_id", nullable = false)
    private Vaccine componentVaccine;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}