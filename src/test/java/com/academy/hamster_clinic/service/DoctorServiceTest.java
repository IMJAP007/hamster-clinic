package com.academy.hamster_clinic.service;

import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.model.dto.DoctorRequest;
import com.academy.hamster_clinic.model.dto.DoctorResponse;
import com.academy.hamster_clinic.model.entity.Doctor;
import com.academy.hamster_clinic.model.repository.DoctorRepository;
import com.academy.hamster_clinic.model.service.DoctorService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock DoctorRepository doctorRepository;

    @InjectMocks DoctorService doctorService;

    // create

    @Test
    @DisplayName("TC-S-DOC-001: create with valid data")
    void create_returnsResponse_whenEmailNotTaken() {
        DoctorRequest req = new DoctorRequest("Иванов", "Терапевт", "+7", "i@vet.example");
        when(doctorRepository.existsByEmail("i@vet.example")).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> {
            Doctor d = inv.getArgument(0);
            d.setId(1L);
            d.setCreatedAt(OffsetDateTime.now());
            return d;
        });

        DoctorResponse resp = doctorService.create(req);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.fullName()).isEqualTo("Иванов");
        assertThat(resp.specialty()).isEqualTo("Терапевт");
        assertThat(resp.email()).isEqualTo("i@vet.example");

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        assertThat(captor.getValue().getSpecialty()).isEqualTo("Терапевт");
    }

    @Test
    @DisplayName("TC-S-DOC-002: create with taken email throws")
    void create_throws_whenEmailTaken() {
        DoctorRequest req = new DoctorRequest("Иванов", "Терапевт", null, "taken@vet.example");
        when(doctorRepository.existsByEmail("taken@vet.example")).thenReturn(true);

        assertThatThrownBy(() -> doctorService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("taken@vet.example");

        verify(doctorRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-S-DOC-003: create with null email skips uniqueness check")
    void create_skipsCheck_whenEmailNull() {
        DoctorRequest req = new DoctorRequest("Иванов", "Терапевт", "+7", null);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> {
            Doctor d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        DoctorResponse resp = doctorService.create(req);

        assertThat(resp.email()).isNull();
        verify(doctorRepository, never()).existsByEmail(any());
        verify(doctorRepository).save(any(Doctor.class));
    }

    // get

    @Test
    @DisplayName("TC-S-DOC-004: get returns DTO when exists")
    void get_returnsResponse_whenExists() {
        Doctor d = doctor(1L, "Иванов", "Терапевт", "i@vet.example");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(d));

        DoctorResponse resp = doctorService.get(1L);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.fullName()).isEqualTo("Иванов");
        assertThat(resp.specialty()).isEqualTo("Терапевт");
    }

    @Test
    @DisplayName("TC-S-DOC-005: get throws when missing")
    void get_throws_whenMissing() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.get(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // list by specialty

    @Test
    @DisplayName("TC-S-DOC-006: list without filter uses findAll")
    void list_returnsAll_whenSpecialtyNull() {
        when(doctorRepository.findAll()).thenReturn(List.of(
                doctor(1L, "Иванов", "Терапевт", null),
                doctor(2L, "Петрова", "Хирург", null)));

        List<DoctorResponse> result = doctorService.list(null);

        assertThat(result).hasSize(2);
        verify(doctorRepository).findAll();
        verify(doctorRepository, never()).findBySpecialtyIgnoreCase(any());
    }

    @Test
    @DisplayName("TC-S-DOC-007: list with blank specialty falls back to findAll")
    void list_returnsAll_whenSpecialtyBlank() {
        when(doctorRepository.findAll()).thenReturn(List.of());

        doctorService.list("   ");

        verify(doctorRepository).findAll();
        verify(doctorRepository, never()).findBySpecialtyIgnoreCase(any());
    }

    @Test
    @DisplayName("TC-S-DOC-009: list with specialty delegates to finder")
    void list_filtersBySpecialty() {
        when(doctorRepository.findBySpecialtyIgnoreCase("Терапевт"))
                .thenReturn(List.of(doctor(1L, "Иванов", "Терапевт", null)));

        List<DoctorResponse> result = doctorService.list("Терапевт");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).specialty()).isEqualTo("Терапевт");
        verify(doctorRepository).findBySpecialtyIgnoreCase("Терапевт");
        verify(doctorRepository, never()).findAll();
    }

    // searchByFullName

    @Test
    @DisplayName("TC-S-DOC-006b: searchByFullName without filter uses findAll")
    void searchByFullName_returnsAll_whenFilterNull() {
        when(doctorRepository.findAll()).thenReturn(List.of(
                doctor(1L, "Иванов", "Терапевт", null)));

        List<DoctorResponse> result = doctorService.searchByFullName(null);

        assertThat(result).hasSize(1);
        verify(doctorRepository).findAll();
        verify(doctorRepository, never()).findByFullNameContainingIgnoreCase(any());
    }

    @Test
    @DisplayName("TC-S-DOC-007b: searchByFullName with blank filter falls back to findAll")
    void searchByFullName_returnsAll_whenFilterBlank() {
        when(doctorRepository.findAll()).thenReturn(List.of());

        doctorService.searchByFullName("   ");

        verify(doctorRepository).findAll();
        verify(doctorRepository, never()).findByFullNameContainingIgnoreCase(any());
    }

    @Test
    @DisplayName("TC-S-DOC-008: searchByFullName with filter delegates to finder")
    void searchByFullName_filtersByFullName() {
        when(doctorRepository.findByFullNameContainingIgnoreCase("Иван"))
                .thenReturn(List.of(doctor(1L, "Иванов Иван Иванович", "Терапевт", null)));

        List<DoctorResponse> result = doctorService.searchByFullName("Иван");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).fullName()).isEqualTo("Иванов Иван Иванович");
        verify(doctorRepository).findByFullNameContainingIgnoreCase("Иван");
        verify(doctorRepository, never()).findAll();
    }

    // update

    @Test
    @DisplayName("TC-S-DOC-010: update changes fields, no explicit save")
    void update_changesFields() {
        Doctor existing = doctor(1L, "Иванов", "Терапевт", "i@vet.example");
        DoctorRequest req = new DoctorRequest("Петров", "Хирург", "+7-902", "p@vet.example");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(doctorRepository.existsByEmail("p@vet.example")).thenReturn(false);

        DoctorResponse resp = doctorService.update(1L, req);

        assertThat(resp.fullName()).isEqualTo("Петров");
        assertThat(resp.specialty()).isEqualTo("Хирург");
        assertThat(resp.email()).isEqualTo("p@vet.example");
        assertThat(existing.getFullName()).isEqualTo("Петров");    // dirty checking
        verify(doctorRepository).existsByEmail("p@vet.example");
        verify(doctorRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-S-DOC-011: update with same email skips uniqueness check")
    void update_skipsCheck_whenSameEmail() {
        Doctor existing = doctor(1L, "Иванов", "Терапевт", "i@vet.example");
        DoctorRequest req = new DoctorRequest("Иванов Новый", "Терапевт", null, "i@vet.example");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));

        doctorService.update(1L, req);

        verify(doctorRepository, never()).existsByEmail(any());
    }

    @Test
    @DisplayName("TC-S-DOC-012: update with taken email throws")
    void update_throws_whenNewEmailTaken() {
        Doctor existing = doctor(1L, "Иванов", "Терапевт", "i@vet.example");
        DoctorRequest req = new DoctorRequest("Иванов", "Терапевт", null, "taken@vet.example");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(doctorRepository.existsByEmail("taken@vet.example")).thenReturn(true);

        assertThatThrownBy(() -> doctorService.update(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("занят");
    }

    @Test
    @DisplayName("TC-S-DOC-013: update throws when doctor missing")
    void update_throws_whenMissing() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.update(999L,
                new DoctorRequest("Иванов", "Терапевт", null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // delete

    @Test
    @DisplayName("TC-S-DOC-014: delete removes entity when exists")
    void delete_removes_whenExists() {
        Doctor d = doctor(1L, "Иванов", "Терапевт", null);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(d));

        doctorService.delete(1L);

        verify(doctorRepository).delete(d);
    }

    @Test
    @DisplayName("TC-S-DOC-015: delete throws when doctor missing")
    void delete_throws_whenMissing() {
        when(doctorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doctorService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(doctorRepository, never()).delete(any());
    }

    // helpers

    private static Doctor doctor(Long id, String fullName, String specialty, String email) {
        Doctor d = new Doctor();
        d.setId(id);
        d.setFullName(fullName);
        d.setSpecialty(specialty);
        d.setEmail(email);
        return d;
    }
}
