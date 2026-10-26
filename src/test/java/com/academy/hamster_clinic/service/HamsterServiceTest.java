package com.academy.hamster_clinic.service;

import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.model.dto.HamsterRenameRequest;
import com.academy.hamster_clinic.model.dto.HamsterRequest;
import com.academy.hamster_clinic.model.dto.HamsterResponse;
import com.academy.hamster_clinic.model.entity.Hamster;
import com.academy.hamster_clinic.model.entity.Owner;
import com.academy.hamster_clinic.model.repository.HamsterRepository;
import com.academy.hamster_clinic.model.repository.OwnerRepository;
import com.academy.hamster_clinic.model.service.HamsterService;

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
class HamsterServiceTest {

    @Mock HamsterRepository hamsterRepository;
    @Mock OwnerRepository ownerRepository;

    @InjectMocks HamsterService hamsterService;

    // create

    @Test
    @DisplayName("TC-S-HAM-001: create with valid data returns DTO")
    void create_returnsResponse_whenValid() {
        Owner owner = owner(1L, "Кузнецова");
        HamsterRequest req = new HamsterRequest("Бублик", 1L, null);
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(hamsterRepository.save(any(Hamster.class))).thenAnswer(inv -> {
            Hamster h = inv.getArgument(0);
            h.setId(10L);
            h.setCreatedAt(OffsetDateTime.now());
            return h;
        });

        HamsterResponse resp = hamsterService.create(req);

        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.name()).isEqualTo("Бублик");
        assertThat(resp.ownerId()).isEqualTo(1L);
        assertThat(resp.ownerFullName()).isEqualTo("Кузнецова");
        assertThat(resp.imageUrl()).isNull();

        ArgumentCaptor<Hamster> captor = ArgumentCaptor.forClass(Hamster.class);
        verify(hamsterRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Бублик");
        assertThat(captor.getValue().getOwner()).isSameAs(owner);
    }

    @Test
    @DisplayName("TC-S-HAM-002: create throws when owner missing")
    void create_throws_whenOwnerMissing() {
        when(ownerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hamsterService.create(
                new HamsterRequest("Бублик", 999L, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(hamsterRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-S-HAM-003: create with null imageUrl → null in DTO")
    void create_allowsNullImageUrl() {
        Owner owner = owner(1L, "Кузнецова");
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(hamsterRepository.save(any(Hamster.class))).thenAnswer(inv -> {
            Hamster h = inv.getArgument(0);
            h.setId(1L);
            return h;
        });

        HamsterResponse resp = hamsterService.create(new HamsterRequest("Бублик", 1L, null));

        assertThat(resp.imageUrl()).isNull();
    }

    @Test
    @DisplayName("TC-S-HAM-004: create with blank imageUrl → normalized to null")
    void create_normalizesBlankImageUrlToNull() {
        Owner owner = owner(1L, "Кузнецова");
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(hamsterRepository.save(any(Hamster.class))).thenAnswer(inv -> {
            Hamster h = inv.getArgument(0);
            h.setId(1L);
            return h;
        });

        hamsterService.create(new HamsterRequest("Бублик", 1L, "   "));

        ArgumentCaptor<Hamster> captor = ArgumentCaptor.forClass(Hamster.class);
        verify(hamsterRepository).save(captor.capture());
        assertThat(captor.getValue().getImageUrl()).isNull();
    }

    @Test
    @DisplayName("TC-S-HAM-005: create trims imageUrl")
    void create_trimsImageUrl() {
        Owner owner = owner(1L, "Кузнецова");
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(hamsterRepository.save(any(Hamster.class))).thenAnswer(inv -> {
            Hamster h = inv.getArgument(0);
            h.setId(1L);
            return h;
        });

        HamsterResponse resp = hamsterService.create(
                new HamsterRequest("Бублик", 1L, "  https://x.com/h.jpg  "));

        assertThat(resp.imageUrl()).isEqualTo("https://x.com/h.jpg");
    }

    @Test
    @DisplayName("TC-S-HAM-006: create maps ownerFullName from entity")
    void create_mapsOwnerFullName() {
        Owner owner = owner(1L, "Кузнецова Анна Петровна");
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(hamsterRepository.save(any(Hamster.class))).thenAnswer(inv -> {
            Hamster h = inv.getArgument(0);
            h.setId(1L);
            return h;
        });

        HamsterResponse resp = hamsterService.create(new HamsterRequest("Бублик", 1L, null));

        assertThat(resp.ownerFullName()).isEqualTo("Кузнецова Анна Петровна");
    }

    // get

    @Test
    @DisplayName("TC-S-HAM-007: get returns DTO when hamster exists")
    void get_returnsResponse_whenExists() {
        Hamster h = hamster(1L, "Бублик", owner(1L, "Кузнецова"));
        when(hamsterRepository.findById(1L)).thenReturn(Optional.of(h));

        HamsterResponse resp = hamsterService.get(1L);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.name()).isEqualTo("Бублик");
        assertThat(resp.ownerId()).isEqualTo(1L);
        assertThat(resp.ownerFullName()).isEqualTo("Кузнецова");
    }

    @Test
    @DisplayName("TC-S-HAM-008: get throws when hamster missing")
    void get_throws_whenMissing() {
        when(hamsterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hamsterService.get(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // list

    @Test
    @DisplayName("TC-S-HAM-009: list without filter uses findAll")
    void list_returnsAll_whenFilterNull() {
        when(hamsterRepository.findAll()).thenReturn(List.of(
                hamster(1L, "Бублик", owner(1L, "Кузнецова")),
                hamster(2L, "Хома", owner(1L, "Кузнецова"))));

        List<HamsterResponse> result = hamsterService.list(null);

        assertThat(result).hasSize(2);
        verify(hamsterRepository).findAll();
        verify(hamsterRepository, never()).findByOwnerFullNameContainingIgnoreCase(any());
    }

    @Test
    @DisplayName("TC-S-HAM-010: list with blank filter falls back to findAll")
    void list_returnsAll_whenFilterBlank() {
        when(hamsterRepository.findAll()).thenReturn(List.of());

        hamsterService.list("   ");

        verify(hamsterRepository).findAll();
        verify(hamsterRepository, never()).findByOwnerFullNameContainingIgnoreCase(any());
    }

    @Test
    @DisplayName("TC-S-HAM-011: list with filter trims and delegates")
    void list_filtersByOwnerFullName() {
        when(hamsterRepository.findByOwnerFullNameContainingIgnoreCase("Кузн"))
                .thenReturn(List.of(hamster(1L, "Бублик", owner(1L, "Кузнецова"))));

        List<HamsterResponse> result = hamsterService.list("  Кузн  ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).ownerFullName()).isEqualTo("Кузнецова");
        verify(hamsterRepository).findByOwnerFullNameContainingIgnoreCase("Кузн");
        verify(hamsterRepository, never()).findAll();
    }

    // rename

    @Test
    @DisplayName("TC-S-HAM-012: rename mutates entity, no explicit save")
    void rename_updatesName() {
        Hamster existing = hamster(1L, "Бублик", owner(1L, "Кузнецова"));
        when(hamsterRepository.findById(1L)).thenReturn(Optional.of(existing));

        HamsterResponse resp = hamsterService.rename(1L, new HamsterRenameRequest("Хома"));

        assertThat(resp.name()).isEqualTo("Хома");
        assertThat(existing.getName()).isEqualTo("Хома");   // dirty checking
        verify(hamsterRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-S-HAM-013: rename throws when hamster missing")
    void rename_throws_whenMissing() {
        when(hamsterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hamsterService.rename(999L,
                new HamsterRenameRequest("Хома")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // delete

    @Test
    @DisplayName("TC-S-HAM-014: delete removes entity when exists")
    void delete_removes_whenExists() {
        Hamster h = hamster(1L, "Бублик", owner(1L, "Кузнецова"));
        when(hamsterRepository.findById(1L)).thenReturn(Optional.of(h));

        hamsterService.delete(1L);

        verify(hamsterRepository).delete(h);
    }

    @Test
    @DisplayName("TC-S-HAM-015: delete throws when hamster missing")
    void delete_throws_whenMissing() {
        when(hamsterRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hamsterService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(hamsterRepository, never()).delete(any());
    }

    // helpers

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
}
