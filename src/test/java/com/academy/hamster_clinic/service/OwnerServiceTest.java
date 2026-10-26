package com.academy.hamster_clinic.service;

import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.model.dto.OwnerRequest;
import com.academy.hamster_clinic.model.dto.OwnerResponse;
import com.academy.hamster_clinic.model.entity.Hamster;
import com.academy.hamster_clinic.model.entity.Owner;
import com.academy.hamster_clinic.model.repository.OwnerRepository;
import com.academy.hamster_clinic.model.service.OwnerService;

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
class OwnerServiceTest {

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private OwnerService ownerService;

    
    // create

    @Test
    @DisplayName("TC-S-OWN-001: create with valid data")
    void create_returnsResponse_whenEmailNotTaken() {
        OwnerRequest req = new OwnerRequest("Анна", "+7", "a@b.c");
        when(ownerRepository.existsByEmail("a@b.c")).thenReturn(false);
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> {
            Owner o = inv.getArgument(0);
            o.setId(1L);
            o.setCreatedAt(OffsetDateTime.now());
            return o;
        });

        OwnerResponse resp = ownerService.create(req);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.fullName()).isEqualTo("Анна");
        assertThat(resp.phone()).isEqualTo("+7");
        assertThat(resp.email()).isEqualTo("a@b.c");
        assertThat(resp.createdAt()).isNotNull();

        ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);
        verify(ownerRepository).save(captor.capture());
        assertThat(captor.getValue().getFullName()).isEqualTo("Анна");
        assertThat(captor.getValue().getEmail()).isEqualTo("a@b.c");
    }

    @Test
    @DisplayName("TC-S-OWN-002: create with null email skips uniqueness check")
    void create_skipsUniquenessCheck_whenEmailNull() {
        OwnerRequest req = new OwnerRequest("Анна", "+7", null);
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> {
            Owner o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        OwnerResponse resp = ownerService.create(req);

        assertThat(resp.email()).isNull();
        verify(ownerRepository, never()).existsByEmail(any());
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    @DisplayName("TC-S-OWN-003: create with taken email throws 409-equivalent")
    void create_throws_whenEmailTaken() {
        OwnerRequest req = new OwnerRequest("Анна", "+7", "taken@b.c");
        when(ownerRepository.existsByEmail("taken@b.c")).thenReturn(true);

        assertThatThrownBy(() -> ownerService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("taken@b.c");

        verify(ownerRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-S-OWN-004: create with null phone is allowed")
    void create_allowsNullPhone() {
        OwnerRequest req = new OwnerRequest("Анна", null, "a@b.c");
        when(ownerRepository.existsByEmail("a@b.c")).thenReturn(false);
        when(ownerRepository.save(any(Owner.class))).thenAnswer(inv -> {
            Owner o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        OwnerResponse resp = ownerService.create(req);

        assertThat(resp.phone()).isNull();
    }

    
    // get

    @Test
    @DisplayName("TC-S-OWN-005: get returns DTO when entity exists")
    void get_returnsResponse_whenExists() {
        Owner o = owner(1L, "Анна", "+7", "a@b.c");
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(o));

        OwnerResponse resp = ownerService.get(1L);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.fullName()).isEqualTo("Анна");
        assertThat(resp.email()).isEqualTo("a@b.c");
    }

    @Test
    @DisplayName("TC-S-OWN-006: get throws ResourceNotFoundException when missing")
    void get_throws_whenMissing() {
        when(ownerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.get(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }


    // list

    @Test
    @DisplayName("TC-S-OWN-007: list without filter uses findAll")
    void list_returnsAll_whenFilterNull() {
        when(ownerRepository.findAll()).thenReturn(List.of(
                owner(1L, "Анна", null, null),
                owner(2L, "Олег", null, null)));

        List<OwnerResponse> result = ownerService.findByFullName(null);

        assertThat(result).hasSize(2);
        verify(ownerRepository).findAll();
        verify(ownerRepository, never()).findByFullNameContainingIgnoreCase(any());
    }

    @Test
    @DisplayName("TC-S-OWN-008: list with blank filter falls back to findAll")
    void list_returnsAll_whenFilterBlank() {
        when(ownerRepository.findAll()).thenReturn(List.of());

        ownerService.findByFullName("   ");

        verify(ownerRepository).findAll();
        verify(ownerRepository, never()).findByFullNameContainingIgnoreCase(any());
    }

    @Test
    @DisplayName("TC-S-OWN-009: list with filter uses finder")
    void list_filtersByFullName() {
        when(ownerRepository.findByFullNameContainingIgnoreCase("Анн"))
                .thenReturn(List.of(owner(1L, "Анна", null, null)));

        List<OwnerResponse> result = ownerService.findByFullName("Анн");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).fullName()).isEqualTo("Анна");
        verify(ownerRepository, never()).findAllByOrderByFullNameAsc();
    }

    @Test
    @DisplayName("TC-S-OWN-010: list maps every field to DTO")
    void list_mapsAllFields() {
        Owner o = owner(7L, "Кузнецова Анна", "+7-901", "k@b.c");
        o.setCreatedAt(OffsetDateTime.parse("2026-10-26T12:00:00+03:00"));
        when(ownerRepository.findAll()).thenReturn(List.of(o));

        OwnerResponse resp = ownerService.findByFullName(null).get(0);

        assertThat(resp.id()).isEqualTo(7L);
        assertThat(resp.fullName()).isEqualTo("Кузнецова Анна");
        assertThat(resp.phone()).isEqualTo("+7-901");
        assertThat(resp.email()).isEqualTo("k@b.c");
        assertThat(resp.createdAt()).isEqualTo(o.getCreatedAt());
    }

    
    // update

    @Test
    @DisplayName("TC-S-OWN-011: update changes fields, no explicit save")
    void update_changesFields() {
        Owner existing = owner(1L, "Анна", "+7", "anna@b.c");
        OwnerRequest req = new OwnerRequest("Анна Новая", "+7-902", "anna.new@b.c");
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ownerRepository.existsByEmail("anna.new@b.c")).thenReturn(false);

        OwnerResponse resp = ownerService.update(1L, req);

        assertThat(resp.fullName()).isEqualTo("Анна Новая");
        assertThat(resp.phone()).isEqualTo("+7-902");
        assertThat(resp.email()).isEqualTo("anna.new@b.c");
        verify(ownerRepository).existsByEmail("anna.new@b.c");
    }

    @Test
    @DisplayName("TC-S-OWN-012: update with same email skips uniqueness check")
    void update_skipsCheck_whenSameEmail() {
        Owner existing = owner(1L, "Анна", null, "anna@b.c");
        OwnerRequest req = new OwnerRequest("Анна", null, "anna@b.c");
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(existing));

        ownerService.update(1L, req);

        verify(ownerRepository, never()).existsByEmail(any());
    }

    @Test
    @DisplayName("TC-S-OWN-013: update with taken email throws")
    void update_throws_whenNewEmailTaken() {
        Owner existing = owner(1L, "Анна", null, "anna@b.c");
        OwnerRequest req = new OwnerRequest("Анна", null, "taken@b.c");
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ownerRepository.existsByEmail("taken@b.c")).thenReturn(true);

        assertThatThrownBy(() -> ownerService.update(1L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("занят");
    }

    @Test
    @DisplayName("TC-S-OWN-014: update throws when owner missing")
    void update_throws_whenMissing() {
        when(ownerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.update(999L, new OwnerRequest("X", null, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("TC-S-OWN-015: update relies on dirty checking, no save() call")
    void update_usesDirtyChecking() {
        Owner existing = owner(1L, "Анна", null, "anna@b.c");
        OwnerRequest req = new OwnerRequest("Новое", "+7", null);
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(existing));

        ownerService.update(1L, req);

        assertThat(existing.getFullName()).isEqualTo("Новое");
        assertThat(existing.getPhone()).isEqualTo("+7");
        assertThat(existing.getEmail()).isNull();
        verify(ownerRepository, never()).save(any());
    }

    
    // delete

    @Test
    @DisplayName("TC-S-OWN-016: delete works when no hamsters")
    void delete_removesOwner_whenNoHamsters() {
        Owner o = owner(1L, "Анна", null, null);
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(o));

        ownerService.delete(1L);

        verify(ownerRepository).delete(o);
    }

    @Test
    @DisplayName("TC-S-OWN-017: delete throws when owner has hamsters")
    void delete_throws_whenOwnerHasHamsters() {
        Owner o = owner(1L, "Анна", null, null);
        Hamster h = new Hamster();
        h.setId(1L);
        o.getHamsters().add(h);
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(o));

        assertThatThrownBy(() -> ownerService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("хомяки");

        verify(ownerRepository, never()).delete(any());
    }

    @Test
    @DisplayName("TC-S-OWN-018: delete throws when owner missing")
    void delete_throws_whenMissing() {
        when(ownerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(ownerRepository, never()).delete(any());
    }

    
    // helpers

    private static Owner owner(Long id, String fullName, String phone, String email) {
        Owner o = new Owner();
        o.setId(id);
        o.setFullName(fullName);
        o.setPhone(phone);
        o.setEmail(email);
        return o;
    }
}