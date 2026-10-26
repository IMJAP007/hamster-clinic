package com.academy.hamster_clinic.service;

import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.model.dto.DiseaseRequest;
import com.academy.hamster_clinic.model.dto.DiseaseResponse;
import com.academy.hamster_clinic.model.entity.Disease;
import com.academy.hamster_clinic.model.repository.DiseaseRepository;
import com.academy.hamster_clinic.model.service.DiseaseService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiseaseServiceTest {

    @Mock DiseaseRepository diseaseRepository;

    @InjectMocks DiseaseService diseaseService;

    // create

    @Test
    @DisplayName("TC-S-DIS-001: create with valid name")
    void create_returnsResponse_whenNameNotTaken() {
        DiseaseRequest req = new DiseaseRequest("Ожирение");
        when(diseaseRepository.existsByNameIgnoreCase("Ожирение")).thenReturn(false);
        when(diseaseRepository.save(any(Disease.class))).thenAnswer(inv -> {
            Disease d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        DiseaseResponse resp = diseaseService.create(req);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.name()).isEqualTo("Ожирение");
        verify(diseaseRepository).existsByNameIgnoreCase("Ожирение");
        verify(diseaseRepository).save(any(Disease.class));
    }

    @Test
    @DisplayName("TC-S-DIS-002: create duplicate with exact case throws")
    void create_throws_whenDuplicateExactCase() {
        DiseaseRequest req = new DiseaseRequest("Ожирение");
        when(diseaseRepository.existsByNameIgnoreCase("Ожирение")).thenReturn(true);

        assertThatThrownBy(() -> diseaseService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ожирение");

        verify(diseaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-S-DIS-003: create duplicate with different case throws")
    void create_throws_whenDuplicateDifferentCase() {
        // В БД уже есть "Ожирение", клиент пытается создать "ожирение".
        // existsByNameIgnoreCase реализован через LOWER(name) = LOWER(?), поэтому ловит и это.
        DiseaseRequest req = new DiseaseRequest("ожирение");
        when(diseaseRepository.existsByNameIgnoreCase("ожирение")).thenReturn(true);

        assertThatThrownBy(() -> diseaseService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ожирение");

        verify(diseaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-S-DIS-008: create saves entity with exact name (no trim, no normalization)")
    void create_savesExactName() {
        DiseaseRequest req = new DiseaseRequest("  Мокрый хвост  ");   // с пробелами
        when(diseaseRepository.existsByNameIgnoreCase("  Мокрый хвост  ")).thenReturn(false);
        when(diseaseRepository.save(any(Disease.class))).thenAnswer(inv -> {
            Disease d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        diseaseService.create(req);

        ArgumentCaptor<Disease> captor = ArgumentCaptor.forClass(Disease.class);
        verify(diseaseRepository).save(captor.capture());

        // Сервис НЕ тримит — сохраняет как есть.
        // Это фиксирует текущее поведение; если добавят trim, тест упадёт.
        assertThat(captor.getValue().getName()).isEqualTo("  Мокрый хвост  ");
    }

    // get

    @Test
    @DisplayName("TC-S-DIS-004: get returns DTO when exists")
    void get_returnsResponse_whenExists() {
        Disease d = disease(1L, "Ожирение");
        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(d));

        DiseaseResponse resp = diseaseService.get(1L);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.name()).isEqualTo("Ожирение");
    }

    @Test
    @DisplayName("TC-S-DIS-005: get throws 404 when missing")
    void get_throws_whenMissing() {
        when(diseaseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diseaseService.get(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // list — без фильтра

    @Test
    @DisplayName("TC-S-DIS-006: list without filter uses findAll")
    void list_returnsAll_whenNameNull() {
        when(diseaseRepository.findAll()).thenReturn(List.of(
                disease(1L, "Ожирение"),
                disease(2L, "Дерматит")));

        List<DiseaseResponse> result = diseaseService.list(null);

        assertThat(result).hasSize(2);
        verify(diseaseRepository).findAll();
        verify(diseaseRepository, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    @DisplayName("TC-S-DIS-007: list maps each entity to DTO")
    void list_mapsEachEntity() {
        when(diseaseRepository.findAll()).thenReturn(List.of(
                disease(1L, "Ожирение"),
                disease(2L, "Дерматит")));

        List<DiseaseResponse> result = diseaseService.list(null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Ожирение");
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).name()).isEqualTo("Дерматит");
    }

    // list — с фильтром

    @Test
    @DisplayName("TC-S-DIS-009: list with blank filter falls back to findAll")
    void list_returnsAll_whenNameBlank() {
        when(diseaseRepository.findAll()).thenReturn(List.of());

        diseaseService.list("   ");

        verify(diseaseRepository).findAll();
        verify(diseaseRepository, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    @DisplayName("TC-S-DIS-010: list with filter delegates to findByNameContainingIgnoreCase")
    void list_filtersByName() {
        when(diseaseRepository.findByNameContainingIgnoreCase("Ожир"))
                .thenReturn(List.of(disease(1L, "Ожирение")));

        List<DiseaseResponse> result = diseaseService.list("Ожир");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Ожирение");
        verify(diseaseRepository).findByNameContainingIgnoreCase("Ожир");
        verify(diseaseRepository, never()).findAll();
    }

    @Test
    @DisplayName("TC-S-DIS-011: list does NOT trim the filter (current behavior)")
    void list_doesNotTrimFilter() {
        // В отличие от AppointmentService.normalize() — DiseaseService не тримит.
        when(diseaseRepository.findByNameContainingIgnoreCase("  Ожир  "))
                .thenReturn(List.of());

        diseaseService.list("  Ожир  ");

        // Пробелы доходят до репозитория. Фиксируем текущее поведение.
        // Если кто-то добавит .trim() в сервис — тест упадёт, и это сигнал обновить протокол.
        verify(diseaseRepository).findByNameContainingIgnoreCase("  Ожир  ");
    }

    // helpers

    private static Disease disease(Long id, String name) {
        Disease d = new Disease();
        d.setId(id);
        d.setName(name);
        return d;
    }
}
