package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.response.VaccineResponse;
import com.healthcare.childrecords.entity.Vaccine;
import com.healthcare.childrecords.repository.VaccineCombinationRepository;
import com.healthcare.childrecords.repository.VaccineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VaccineService {

    private final VaccineRepository vaccineRepository;
    private final VaccineCombinationRepository vaccineCombinationRepository;

    public List<VaccineResponse> getAllVaccines() {
        return vaccineRepository.findAll().stream()
                .map(this::toResponse)
                .sorted((a, b) -> a.vaccineName().compareTo(b.vaccineName()))
                .toList();
    }

    private VaccineResponse toResponse(Vaccine vaccine) {
        List<String> components = vaccineCombinationRepository.findComponentsByVaccineId(vaccine.getId()).stream()
                .map(Vaccine::getVaccineName)
                .toList();
        return new VaccineResponse(vaccine.getId(), vaccine.getVaccineName(), vaccine.getVaccineNameGr(), components);
    }
}
