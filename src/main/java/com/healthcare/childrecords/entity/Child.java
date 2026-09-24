package com.healthcare.childrecords.entity;

import com.healthcare.childrecords.util.EncryptionConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "children")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Child {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "first_name", nullable = false, columnDefinition = "TEXT")
    private String firstName;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "last_name", nullable = false, columnDefinition = "TEXT")
    private String lastName;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "amka", nullable = false, unique = true, length = 255)
    private String amka;

    @Column(name = "amka_hash", nullable = false, unique = true, length = 64)
    private String amkaHash;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "date_of_birth", nullable = false, columnDefinition = "TEXT")
    private String dateOfBirth;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "blood_type", columnDefinition = "TEXT")
    private String bloodType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "child_doctor",
        joinColumns = @JoinColumn(name = "child_id"),
        inverseJoinColumns = @JoinColumn(name = "doctor_id")
    )
    @Builder.Default
    private List<Doctor> doctors = new ArrayList<>();

    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Vaccination> vaccinations = new ArrayList<>();

    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Allergy> allergies = new ArrayList<>();

    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<GrowthMeasurement> growthMeasurements = new ArrayList<>();

    @OneToOne(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BirthRecord birthRecord;

}
