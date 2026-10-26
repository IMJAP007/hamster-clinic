package com.academy.hamster_clinic.service;

import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.model.dto.HamsterDiseaseRequest;
import com.academy.hamster_clinic.model.dto.HamsterDiseaseResponse;
import com.academy.hamster_clinic.model.entity.Disease;
import com.academy.hamster_clinic.model.entity.Hamster;
import com.academy.hamster_clinic.model.entity.HamsterDisease;
import com.academy.hamster_clinic.model.repository.DiseaseRepository;
import com.academy.hamster_clinic.model.repository.HamsterDiseaseRepository;
import com.academy.hamster_clinic.model.repository.HamsterRepository;
import com.academy.hamster_clinic.model.service.HamsterDiseaseService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HamsterDiseaseServiceTest {

    @Mock HamsterDiseaseRepository hamsterDiseaseRepository;
    @Mock HamsterRepository hamsterRepository;
    @Mock DiseaseRepository diseaseRepository;

    @InjectMocks HamsterDiseaseService hamsterDiseaseService;

    private Hamster hamster;
    private Disease disease;
    private LocalDate diagnosisDate;

    @BeforeEach
    void setUp() {
        hamster = hamster(1L, "Бублик");
        disease = disease(1L, "Ожирение");
        diagnosisDate = LocalDate.of(2026, 10, 15);
    }

    // create — happy path

    @Test
    @DisplayName("TC-S-HD-001: create with valid data")
    void create_returnsResponse_whenAllValid() {
        HamsterDiseaseRequest req = new HamsterDiseaseRequest(1L, 1L, diagnosisDate);
        when(hamsterDiseaseRepository.existsByHamsterIdAndDiseaseIdAndDiagnosedAt(
                1L, 1L, diagnosisDate)).thenReturn(false);
        when(hamsterRepository.findById(1L)).thenReturn(Optional.of(hamster));
        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(disease));
        when(hamsterDiseaseRepository.save(any(HamsterDisease.class))).thenAnswer(inv -> {
            HamsterDisease hd = inv.getArgument(0);
            hd.setId(50L);
            return hd;
        });

        HamsterDiseaseResponse resp = hamsterDiseaseService.create(req);

        assertThat(resp.id()).isEqualTo(50L);
        assertThat(resp.hamsterId()).isEqualTo(1L);
        assertThat(resp.hamsterName()).isEqualTo("Бублик");
        assertThat(resp.diseaseId()).isEqualTo(1L);
        assertThat(resp.diseaseName()).isEqualTo("Ожирение");
        assertThat(resp.diagnosedAt()).isEqualTo(diagnosisDate);

        ArgumentCaptor<HamsterDisease> captor =
                ArgumentCaptor.forClass(HamsterDisease.class);
        verify(hamsterDiseaseRepository).save(captor.capture());
        assertThat(captor.getValue().getHamster()).isSameAs(hamster);
        assertThat(captor.getValue().getDisease()).isSameAs(disease);
        assertThat(captor.getValue().getDiagnosedAt()).isEqualTo(diagnosisDate);
    }

    // create — 409: duplicate

    @Test
    @DisplayName("TC-S-HD-002: create throws when diagnosis already exists (before loading entities)")
    void create_throws_whenDuplicate_andDoesNotLoadEntities() {
        HamsterDiseaseRequest req = new HamsterDiseaseRequest(1L, 1L, diagnosisDate);
        when(hamsterDiseaseRepository.existsByHamsterIdAndDiseaseIdAndDiagnosedAt(
                1L, 1L, diagnosisDate)).thenReturn(true);

        assertThatThrownBy(() -> hamsterDiseaseService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(diagnosisDate.toString());

        // Ключевая проверка: до загрузки сущностей дело не дошло
        verifyNoInteractions(hamsterRepository, diseaseRepository);
        verify(hamsterDiseaseRepository, never()).save(any());
    }

    // create — 404: hamster / disease

    @Test
    @DisplayName("TC-S-HD-003: create throws 404 when hamster missing")
    void create_throws_whenHamsterMissing() {
        HamsterDiseaseRequest req = new HamsterDiseaseRequest(999L, 1L, diagnosisDate);
        when(hamsterDiseaseRepository.existsByHamsterIdAndDiseaseIdAndDiagnosedAt(
                999L, 1L, diagnosisDate)).thenReturn(false);
        when(hamsterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hamsterDiseaseService.create(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Хомяк")
                .hasMessageContaining("999");

        verifyNoInteractions(diseaseRepository);
        verify(hamsterDiseaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-S-HD-004: create throws 404 when disease missing")
    void create_throws_whenDiseaseMissing() {
        HamsterDiseaseRequest req = new HamsterDiseaseRequest(1L, 999L, diagnosisDate);
        when(hamsterDiseaseRepository.existsByHamsterIdAndDiseaseIdAndDiagnosedAt(
                1L, 999L, diagnosisDate)).thenReturn(false);
        when(hamsterRepository.findById(1L)).thenReturn(Optional.of(hamster));
        when(diseaseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hamsterDiseaseService.create(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Болезнь")
                .hasMessageContaining("999");

        verify(hamsterDiseaseRepository, never()).save(any());
    }

    // create — порядок проверок

    @Test
    @DisplayName("TC-S-HD-004b: hamster is checked before disease")
    void create_checksHamsterBeforeDisease() {
        HamsterDiseaseRequest req = new HamsterDiseaseRequest(1L, 1L, diagnosisDate);
        when(hamsterDiseaseRepository.existsByHamsterIdAndDiseaseIdAndDiagnosedAt(
                1L, 1L, diagnosisDate)).thenReturn(false);
        when(hamsterRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hamsterDiseaseService.create(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Хомяк");

        verifyNoInteractions(diseaseRepository);
    }

    // create — маппинг

    @Test
    @DisplayName("TC-S-HD-005: create maps all response fields")
    void create_mapsResponseFields() {
        HamsterDiseaseRequest req = new HamsterDiseaseRequest(1L, 1L, diagnosisDate);
        when(hamsterDiseaseRepository.existsByHamsterIdAndDiseaseIdAndDiagnosedAt(
                any(), any(), any())).thenReturn(false);
        when(hamsterRepository.findById(1L)).thenReturn(Optional.of(hamster));
        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(disease));
        when(hamsterDiseaseRepository.save(any(HamsterDisease.class))).thenAnswer(inv -> {
            HamsterDisease hd = inv.getArgument(0);
            hd.setId(77L);
            return hd;
        });

        HamsterDiseaseResponse resp = hamsterDiseaseService.create(req);

        assertThat(resp.hamsterName()).isEqualTo("Бублик");
        assertThat(resp.diseaseName()).isEqualTo("Ожирение");
        assertThat(resp.diagnosedAt()).isEqualTo(diagnosisDate);
    }

    // get

    @Test
    @DisplayName("TC-S-HD-006: get returns DTO when exists")
    void get_returnsResponse_whenExists() {
        HamsterDisease hd = hamsterDisease(1L, hamster, disease, diagnosisDate);
        when(hamsterDiseaseRepository.findById(1L)).thenReturn(Optional.of(hd));

        HamsterDiseaseResponse resp = hamsterDiseaseService.get(1L);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.hamsterName()).isEqualTo("Бублик");
        assertThat(resp.diseaseName()).isEqualTo("Ожирение");
        assertThat(resp.diagnosedAt()).isEqualTo(diagnosisDate);
    }

    @Test
    @DisplayName("TC-S-HD-007: get throws 404 when missing")
    void get_throws_whenMissing() {
        when(hamsterDiseaseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hamsterDiseaseService.get(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // list — нормализация и комбинации

    @Test
    @DisplayName("TC-S-HD-008: list with no filters passes nulls")
    void list_passesNulls_whenNoFilters() {
        when(hamsterDiseaseRepository.findByFilters(isNull(), isNull()))
                .thenReturn(List.of());

        hamsterDiseaseService.list(null, null);

        verify(hamsterDiseaseRepository).findByFilters(null, null);
    }

    @Test
    @DisplayName("TC-S-HD-009: list with hamster filter")
    void list_filtersByHamster() {
        when(hamsterDiseaseRepository.findByFilters("Бублик", null))
                .thenReturn(List.of());

        hamsterDiseaseService.list("Бублик", null);

        verify(hamsterDiseaseRepository).findByFilters("Бублик", null);
    }

    @Test
    @DisplayName("TC-S-HD-010: list with disease filter")
    void list_filtersByDisease() {
        when(hamsterDiseaseRepository.findByFilters(null, "Ожирение"))
                .thenReturn(List.of());

        hamsterDiseaseService.list(null, "Ожирение");

        verify(hamsterDiseaseRepository).findByFilters(null, "Ожирение");
    }

    @Test
    @DisplayName("TC-S-HD-011: list with both filters")
    void list_filtersByBoth() {
        when(hamsterDiseaseRepository.findByFilters("Бублик", "Ожирение"))
                .thenReturn(List.of());

        hamsterDiseaseService.list("Бублик", "Ожирение");

        verify(hamsterDiseaseRepository).findByFilters("Бублик", "Ожирение");
    }

    @Test
    @DisplayName("TC-S-HD-012: list normalizes blank strings to null")
    void list_normalizesBlankToNull() {
        when(hamsterDiseaseRepository.findByFilters(isNull(), isNull()))
                .thenReturn(List.of());

        hamsterDiseaseService.list("", "   ");

        verify(hamsterDiseaseRepository).findByFilters(null, null);
    }

    @Test
    @DisplayName("TC-S-HD-013: list trims non-blank inputs")
    void list_trimsInputs() {
        when(hamsterDiseaseRepository.findByFilters("Бублик", "Ожирение"))
                .thenReturn(List.of());

        hamsterDiseaseService.list("  Бублик  ", "  Ожирение  ");

        verify(hamsterDiseaseRepository).findByFilters("Бублик", "Ожирение");
    }

    @Test
    @DisplayName("TC-S-HD-013b: list maps each entity to DTO")
    void list_mapsEachEntity() {
        HamsterDisease hd = hamsterDisease(1L, hamster, disease, diagnosisDate);
        when(hamsterDiseaseRepository.findByFilters(any(), any()))
                .thenReturn(List.of(hd));

        List<HamsterDiseaseResponse> result = hamsterDiseaseService.list(null, null);

        assertThat(result).hasSize(1);
        HamsterDiseaseResponse resp = result.get(0);
        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.hamsterName()).isEqualTo("Бублик");
        assertThat(resp.diseaseName()).isEqualTo("Ожирение");
        assertThat(resp.diagnosedAt()).isEqualTo(diagnosisDate);
    }

    // delete

    @Test
    @DisplayName("TC-S-HD-014: delete removes entity when exists")
    void delete_removes_whenExists() {
        HamsterDisease hd = hamsterDisease(1L, hamster, disease, diagnosisDate);
        when(hamsterDiseaseRepository.findById(1L)).thenReturn(Optional.of(hd));

        hamsterDiseaseService.delete(1L);

        verify(hamsterDiseaseRepository).delete(hd);
    }

    @Test
    @DisplayName("TC-S-HD-015: delete throws 404 when missing")
    void delete_throws_whenMissing() {
        when(hamsterDiseaseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hamsterDiseaseService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(hamsterDiseaseRepository, never()).delete(any());
    }

    // helpers

    private static Hamster hamster(Long id, String name) {
        Hamster h = new Hamster();
        h.setId(id);
        h.setName(name);
        return h;
    }

    private static Disease disease(Long id, String name) {
        Disease d = new Disease();
        d.setId(id);
        d.setName(name);
        return d;
    }

    private static HamsterDisease hamsterDisease(Long id, Hamster h, Disease d,
                                                 LocalDate diagnosedAt) {
        HamsterDisease hd = new HamsterDisease();
        hd.setId(id);
        hd.setHamster(h);
        hd.setDisease(d);
        hd.setDiagnosedAt(diagnosedAt);
        return hd;
    }
}
