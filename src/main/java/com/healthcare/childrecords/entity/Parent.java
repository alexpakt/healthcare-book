package com.healthcare.childrecords.entity;

import com.healthcare.childrecords.util.EncryptionConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parents")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parent {

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
    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String email;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "phone_number", columnDefinition = "TEXT")
    private String phoneNumber;

    @Convert(converter = EncryptionConverter.class)
    @Column(columnDefinition = "TEXT")
    private String address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Child> children = new ArrayList<>();
}
