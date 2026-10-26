package com.academy.hamster_clinic.service;

import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.model.dto.AppointmentRequest;
import com.academy.hamster_clinic.model.dto.AppointmentResponse;
import com.academy.hamster_clinic.model.entity.Appointment;
import com.academy.hamster_clinic.model.entity.Doctor;
import com.academy.hamster_clinic.model.entity.Hamster;
import com.academy.hamster_clinic.model.entity.Owner;
import com.academy.hamster_clinic.model.repository.AppointmentRepository;
import com.academy.hamster_clinic.model.repository.DoctorRepository;
import com.academy.hamster_clinic.model.repository.HamsterRepository;
import com.academy.hamster_clinic.model.repository.OwnerRepository;
import com.academy.hamster_clinic.model.service.AppointmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock AppointmentRepository appointmentRepository;
    @Mock DoctorRepository doctorRepository;
    @Mock OwnerRepository ownerRepository;
    @Mock HamsterRepository hamsterRepository;

    @InjectMocks AppointmentService appointmentService;

    private Doctor doctor;
    private Owner owner;
    private Hamster hamster;
    private LocalDateTime when;

    @BeforeEach
    void setUp() {
        doctor = doctor(1L, "Иванов Иван Иванович", "Терапевт");
        owner  = owner(1L, "Кузнецова Анна");
        hamster = hamster(1L, "Бублик", owner);
        when = LocalDateTime.of(2026, 11, 1, 10, 30);
    }

    // create — happy path

    @Test
    @DisplayName("TC-S-APT-001: create with valid data")
    void create_returnsResponse_whenAllValid() {
        AppointmentRequest req = new AppointmentRequest(1L, 1L, 1L, when);
        stubAllFound();
        when(appointmentRepository.existsByDoctorIdAndAppointmentDate(1L, when)).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(100L);
            a.setCreatedAt(OffsetDateTime.now());
            return a;
        });

        AppointmentResponse resp = appointmentService.create(req);

        assertThat(resp.id()).isEqualTo(100L);
        assertThat(resp.doctorId()).isEqualTo(1L);
        assertThat(resp.doctorFullName()).isEqualTo("Иванов Иван Иванович");
        assertThat(resp.doctorSpecialty()).isEqualTo("Терапевт");
        assertThat(resp.ownerId()).isEqualTo(1L);
        assertThat(resp.ownerFullName()).isEqualTo("Кузнецова Анна");
        assertThat(resp.hamsterId()).isEqualTo(1L);
        assertThat(resp.hamsterName()).isEqualTo("Бублик");
        assertThat(resp.appointmentDate()).isEqualTo(when);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());
        assertThat(captor.getValue().getDoctor()).isSameAs(doctor);
        assertThat(captor.getValue().getOwner()).isSameAs(owner);
        assertThat(captor.getValue().getHamster()).isSameAs(hamster);
    }

    // create — 404: doctor / owner / hamster

    @Test
    @DisplayName("TC-S-APT-002: create throws 404 when doctor missing")
    void create_throws_whenDoctorMissing() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(
                new AppointmentRequest(1L, 1L, 1L, when)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Врач")
                .hasMessageContaining("1");

        verifyNoInteractions(ownerRepository, hamsterRepository, appointmentRepository);
    }

    @Test
    @DisplayName("TC-S-APT-003: create throws 404 when owner missing")
    void create_throws_whenOwnerMissing() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(ownerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(
                new AppointmentRequest(1L, 1L, 1L, when)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Хозяин");

        verifyNoInteractions(hamsterRepository, appointmentRepository);
    }

    @Test
    @DisplayName("TC-S-APT-004: create throws 404 when hamster missing")
    void create_throws_whenHamsterMissing() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(hamsterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(
                new AppointmentRequest(1L, 1L, 1L, when)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Хомяк");

        verify(appointmentRepository, never()).save(any());
    }

    // create — 409: wrong owner / doctor busy

    @Test
    @DisplayName("TC-S-APT-005: create 409 when hamster belongs to another owner")
    void create_throws_whenHamsterBelongsToAnotherOwner() {
        Owner other = owner(2L, "Смирнов Олег");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(ownerRepository.findById(2L)).thenReturn(Optional.of(other));
        when(hamsterRepository.findById(1L)).thenReturn(Optional.of(hamster)); // owner_id = 1

        assertThatThrownBy(() -> appointmentService.create(
                new AppointmentRequest(1L, 2L, 1L, when)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("не принадлежит");

        verify(appointmentRepository, never()).save(any());
        verify(appointmentRepository, never())
                .existsByDoctorIdAndAppointmentDate(any(), any());
    }

    @Test
    @DisplayName("TC-S-APT-006: create 409 when doctor is busy")
    void create_throws_whenDoctorBusy() {
        stubAllFound();
        when(appointmentRepository.existsByDoctorIdAndAppointmentDate(1L, when)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.create(
                new AppointmentRequest(1L, 1L, 1L, when)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("занят");

        verify(appointmentRepository, never()).save(any());
    }

    // create — порядок проверок

    @Test
    @DisplayName("TC-S-APT-007: doctor is checked before owner")
    void create_checksDoctorFirst() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());
        // ownerRepository.findById даже не должен вызываться

        assertThatThrownBy(() -> appointmentService.create(
                new AppointmentRequest(1L, 1L, 1L, when)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Врач");

        verifyNoInteractions(ownerRepository);
    }

    @Test
    @DisplayName("TC-S-APT-008: hamster is checked after doctor and owner")
    void create_checksHamsterAfterDoctorAndOwner() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(hamsterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.create(
                new AppointmentRequest(1L, 1L, 1L, when)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Хомяк");

        verify(doctorRepository).findById(1L);
        verify(ownerRepository).findById(1L);
    }

    // create — маппинг

    @Test
    @DisplayName("TC-S-APT-009: create maps all response fields")
    void create_mapsResponseFields() {
        stubAllFound();
        when(appointmentRepository.existsByDoctorIdAndAppointmentDate(any(), any())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment a = inv.getArgument(0);
            a.setId(42L);
            return a;
        });

        AppointmentResponse resp = appointmentService.create(
                new AppointmentRequest(1L, 1L, 1L, when));

        assertThat(resp.doctorFullName()).isEqualTo("Иванов Иван Иванович");
        assertThat(resp.doctorSpecialty()).isEqualTo("Терапевт");
        assertThat(resp.ownerFullName()).isEqualTo("Кузнецова Анна");
        assertThat(resp.hamsterName()).isEqualTo("Бублик");
        assertThat(resp.appointmentDate()).isEqualTo(when);
    }

    // get

    @Test
    @DisplayName("TC-S-APT-010: get returns DTO when exists")
    void get_returnsResponse_whenExists() {
        Appointment a = appointment(1L, doctor, owner, hamster, when);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(a));

        AppointmentResponse resp = appointmentService.get(1L);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.doctorId()).isEqualTo(1L);
        assertThat(resp.hamsterName()).isEqualTo("Бублик");
    }

    @Test
    @DisplayName("TC-S-APT-011: get throws 404 when missing")
    void get_throws_whenMissing() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.get(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // list — нормализация и комбинации

    @Test
    @DisplayName("TC-S-APT-012: list with no filters passes nulls")
    void list_passesNulls_whenNoFilters() {
        when(appointmentRepository.findByFilters(isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        appointmentService.list(null, null, null);

        verify(appointmentRepository).findByFilters(null, null, null);
    }

    @Test
    @DisplayName("TC-S-APT-013: list with doctor filter")
    void list_filtersByDoctor() {
        when(appointmentRepository.findByFilters("Иванов", null, null))
                .thenReturn(List.of());

        appointmentService.list("Иванов", null, null);

        verify(appointmentRepository).findByFilters("Иванов", null, null);
    }

    @Test
    @DisplayName("TC-S-APT-014: list with owner filter")
    void list_filtersByOwner() {
        when(appointmentRepository.findByFilters(null, "Кузнецова", null))
                .thenReturn(List.of());

        appointmentService.list(null, "Кузнецова", null);

        verify(appointmentRepository).findByFilters(null, "Кузнецова", null);
    }

    @Test
    @DisplayName("TC-S-APT-015: list with hamster filter")
    void list_filtersByHamster() {
        when(appointmentRepository.findByFilters(null, null, "Бублик"))
                .thenReturn(List.of());

        appointmentService.list(null, null, "Бублик");

        verify(appointmentRepository).findByFilters(null, null, "Бублик");
    }

    @Test
    @DisplayName("TC-S-APT-016: list with all three filters")
    void list_filtersByAll() {
        when(appointmentRepository.findByFilters("Иванов", "Кузнецова", "Бублик"))
                .thenReturn(List.of());

        appointmentService.list("Иванов", "Кузнецова", "Бублик");

        verify(appointmentRepository).findByFilters("Иванов", "Кузнецова", "Бублик");
    }

    @Test
    @DisplayName("TC-S-APT-017: list normalizes blank strings to null")
    void list_normalizesBlankToNull() {
        when(appointmentRepository.findByFilters(isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        appointmentService.list("", "   ", "");

        verify(appointmentRepository).findByFilters(null, null, null);
    }

    @Test
    @DisplayName("TC-S-APT-018: list trims non-blank inputs")
    void list_trimsInputs() {
        when(appointmentRepository.findByFilters("Иванов", "Кузнецова", "Бублик"))
                .thenReturn(List.of());

        appointmentService.list("  Иванов  ", "  Кузнецова  ", "  Бублик  ");

        verify(appointmentRepository).findByFilters("Иванов", "Кузнецова", "Бублик");
    }

    @Test
    @DisplayName("TC-S-APT-018b: list maps each entity to DTO")
    void list_mapsEachEntity() {
        Appointment a = appointment(1L, doctor, owner, hamster, when);
        when(appointmentRepository.findByFilters(any(), any(), any()))
                .thenReturn(List.of(a));

        List<AppointmentResponse> result = appointmentService.list(null, null, null);

        assertThat(result).hasSize(1);
        AppointmentResponse resp = result.get(0);
        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.doctorFullName()).isEqualTo("Иванов Иван Иванович");
        assertThat(resp.hamsterName()).isEqualTo("Бублик");
    }

    // cancel

    @Test
    @DisplayName("TC-S-APT-019: cancel removes entity when exists")
    void cancel_removes_whenExists() {
        Appointment a = appointment(1L, doctor, owner, hamster, when);
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(a));

        appointmentService.cancel(1L);

        verify(appointmentRepository).delete(a);
    }

    @Test
    @DisplayName("TC-S-APT-020: cancel throws 404 when missing")
    void cancel_throws_whenMissing() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.cancel(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(appointmentRepository, never()).delete(any());
    }

    // helpers

    private void stubAllFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(hamsterRepository.findById(1L)).thenReturn(Optional.of(hamster));
    }

    private static Doctor doctor(Long id, String fullName, String specialty) {
        Doctor d = new Doctor();
        d.setId(id);
        d.setFullName(fullName);
        d.setSpecialty(specialty);
        return d;
    }

    private static Owner owner(Long id, String fullName) {
        Owner o = new Owner();
        o.setId(id);
        o.setFullName(fullName);
        return o;
    }

    private static Hamster hamster(Long id, String name, Owner owner) {
        Hamster h = new Hamster();
        h.setId(id);
        h.setName(name);
        h.setOwner(owner);
        return h;
    }

    private static Appointment appointment(Long id, Doctor d, Owner o, Hamster h,
                                           LocalDateTime date) {
        Appointment a = new Appointment();
        a.setId(id);
        a.setDoctor(d);
        a.setOwner(o);
        a.setHamster(h);
        a.setAppointmentDate(date);
        return a;
    }
}
