package com.healthcare.childrecords.service;

import com.healthcare.childrecords.dto.request.VaccinationScheduleRequest;
import com.healthcare.childrecords.dto.response.VaccinationScheduleResponse;
import com.healthcare.childrecords.entity.Vaccine;
import com.healthcare.childrecords.entity.VaccinationSchedule;
import com.healthcare.childrecords.exception.ResourceNotFoundException;
import com.healthcare.childrecords.repository.VaccineRepository;
import com.healthcare.childrecords.repository.VaccinationScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VaccinationScheduleService {

    private final VaccinationScheduleRepository vaccinationScheduleRepository;
    private final VaccineRepository vaccineRepository;

    public List<VaccinationScheduleResponse> getAllSchedules() {
        return vaccinationScheduleRepository.findAllByOrderByRecommendedAgeMonthsAscDoseNumberAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<VaccinationScheduleResponse> getScheduleByVaccineName(String vaccineName) {
        return vaccinationScheduleRepository.findByVaccineName(vaccineName)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<VaccinationScheduleResponse> getSchedulesByAge(Integer ageMonths) {
        return vaccinationScheduleRepository.findByRecommendedAgeMonthsOrderByDoseNumber(ageMonths)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public VaccinationScheduleResponse getScheduleById(Long id) {
        return vaccinationScheduleRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination schedule not found: " + id));
    }

    @Transactional
    public VaccinationScheduleResponse createSchedule(VaccinationScheduleRequest request) {
        Vaccine vaccine = vaccineRepository.findByVaccineName(request.vaccineName())
                .orElseGet(() -> {
                    Vaccine newVaccine = Vaccine.builder()
                            .vaccineName(request.vaccineName())
                            .vaccineNameGr(request.vaccineNameGr())
                            .build();
                    return vaccineRepository.save(newVaccine);
                });

        VaccinationSchedule schedule = VaccinationSchedule.builder()
                .vaccine(vaccine)
                .recommendedAgeMonths(request.recommendedAgeMonths())
                .doseNumber(request.doseNumber())
                .notes(request.notes())
                .build();

        return toResponse(vaccinationScheduleRepository.save(schedule));
    }

    @Transactional
    public VaccinationScheduleResponse updateSchedule(Long id, VaccinationScheduleRequest request) {
        VaccinationSchedule schedule = vaccinationScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination schedule not found: " + id));

        Vaccine vaccine = vaccineRepository.findByVaccineName(request.vaccineName())
                .orElseGet(() -> {
                    Vaccine newVaccine = Vaccine.builder()
                            .vaccineName(request.vaccineName())
                            .vaccineNameGr(request.vaccineNameGr())
                            .build();
                    return vaccineRepository.save(newVaccine);
                });

        schedule.setVaccine(vaccine);
        schedule.setRecommendedAgeMonths(request.recommendedAgeMonths());
        if (request.doseNumber() != null) {
            schedule.setDoseNumber(request.doseNumber());
        }
        if (request.notes() != null) {
            schedule.setNotes(request.notes());
        }

        return toResponse(vaccinationScheduleRepository.save(schedule));
    }

    @Transactional
    public void deleteSchedule(Long id) {
        vaccinationScheduleRepository.deleteById(id);
    }

    private VaccinationScheduleResponse toResponse(VaccinationSchedule entity) {
        return new VaccinationScheduleResponse(
                entity.getId(),
                entity.getVaccine().getId(),
                entity.getVaccine().getVaccineName(),
                entity.getVaccine().getVaccineNameGr(),
                entity.getRecommendedAgeMonths(),
                entity.getDoseNumber(),
                entity.getNotes()
        );
    }
}
