package com.academy.hamster_clinic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academy.hamster_clinic.dto.HamsterRenameRequest;
import com.academy.hamster_clinic.dto.HamsterRequest;
import com.academy.hamster_clinic.dto.HamsterResponse;
import com.academy.hamster_clinic.entity.Hamster;
import com.academy.hamster_clinic.entity.Owner;
import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.repository.HamsterRepository;
import com.academy.hamster_clinic.repository.OwnerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HamsterService {

    private final HamsterRepository hamsterRepository;
    private final OwnerRepository ownerRepository;

        @Transactional
    public HamsterResponse create(HamsterRequest req) {
        Owner owner = ownerRepository.findById(req.ownerId()).orElseThrow(
            () -> new ResourceNotFoundException("Хозяин не найден: id = " + req.ownerId()));
        
            Hamster h = new Hamster();
        
        h.setName(req.name());
        h.setOwner(owner);
        
        return toResponse(hamsterRepository.save(h));
    }

    public HamsterResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<HamsterResponse> list(Long ownerId) {
        List<Hamster> hamsters = (ownerId == null) ? hamsterRepository.findAll() : hamsterRepository.findByOwnerId(ownerId);
        return hamsters.stream().map(HamsterService::toResponse).toList();
    }

    @Transactional
    public HamsterResponse rename(Long id, HamsterRenameRequest req) {
        Hamster h = findOrThrow(id);
        
        h.setName(req.name());
        
        return toResponse(h);
    }

    @Transactional
    public void delete(Long id) {
        hamsterRepository.delete(findOrThrow(id));
    }

    private Hamster findOrThrow(Long id) {
        return hamsterRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Хомяк не найден: id = " + id));
    }

    private static HamsterResponse toResponse(Hamster h) {
        return new HamsterResponse(h.getId(), h.getName(), h.getOwner().getId(), h.getOwner().getFullName(), h.getCreatedAt());
    }
}