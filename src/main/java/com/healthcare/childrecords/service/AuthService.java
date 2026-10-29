package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.ChangePasswordRequest;
import com.healthcare.childrecords.dto.request.LoginRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.dto.response.AuthResult;
import com.healthcare.childrecords.dto.response.CurrentUserResponse;
import com.healthcare.childrecords.entity.*;
import com.healthcare.childrecords.repository.*;
import com.healthcare.childrecords.security.JwtUtil;
import com.healthcare.childrecords.util.AmkaHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final ParentRepository parentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AmkaHashUtil amkaHashUtil;

    @Transactional
    public AuthResult registerDoctor(RegisterDoctorRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Το όνομα χρήστη χρησιμοποιείται ήδη");
        }
        String amkaHash = amkaHashUtil.hashAmka(request.amka());
        if (doctorRepository.existsByAmkaHash(amkaHash)) {
            throw new IllegalArgumentException("Υπάρχει ήδη ιατρός με αυτόν τον ΑΜΚΑ");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_DOCTOR)
                .build();
        user = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .amka(request.amka())
                .amkaHash(amkaHash)
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .specialization(request.specialization())
                .licenseNumber(request.licenseNumber())
                .address(request.address())
                .build();
        doctor = doctorRepository.save(doctor);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return new AuthResult(jwtUtil.generateToken(userDetails), user.getRole().name(), doctor.getId());
    }

    @Transactional
    public AuthResult registerParent(RegisterParentRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Το όνομα χρήστη χρησιμοποιείται ήδη");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_PARENT)
                .build();
        user = userRepository.save(user);

        Parent parent = Parent.builder()
                .user(user)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .build();
        parent = parentRepository.save(parent);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return new AuthResult(jwtUtil.generateToken(userDetails), user.getRole().name(), parent.getId());
    }

    public AuthResult login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        User user = userRepository.findByUsername(request.username()).orElseThrow();

        return new AuthResult(jwtUtil.generateToken(userDetails), user.getRole().name(), findProfileId(user));
    }

    public CurrentUserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Ο χρήστης δεν βρέθηκε"));
        return new CurrentUserResponse(user.getUsername(), user.getRole().name(), findProfileId(user));
    }

    private Long findProfileId(User user) {
        return switch (user.getRole()) {
            case ROLE_DOCTOR -> doctorRepository.findByUserId(user.getId())
                    .map(Doctor::getId).orElse(null);
            case ROLE_PARENT -> parentRepository.findByUserId(user.getId())
                    .map(Parent::getId).orElse(null);
        };
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Ο χρήστης δεν βρέθηκε"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Ο τρέχων κωδικός πρόσβασης είναι λανθασμένος");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
