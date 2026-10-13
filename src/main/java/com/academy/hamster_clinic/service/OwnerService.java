package com.academy.hamster_clinic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.academy.hamster_clinic.dto.OwnerRequest;
import com.academy.hamster_clinic.dto.OwnerResponse;
import com.academy.hamster_clinic.entity.Owner;
import com.academy.hamster_clinic.exception.BusinessException;
import com.academy.hamster_clinic.exception.ResourceNotFoundException;
import com.academy.hamster_clinic.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final OwnerRepository ownerRepository;

    @Transactional
    public OwnerResponse create(OwnerRequest req){
        if (req.email() != null && ownerRepository.existsByEmail(req.email())) {
            throw new BusinessException("Хозяин с таким email уже существует: " + req.email());
        }

        Owner o = new Owner();
        o.setFullName(req.fullName());
        o.setPhone(req.phone());
        o.setEmail(req.email());
        
        return toResponse(ownerRepository.save(o));
    }

    public OwnerResponse get(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<OwnerResponse> list() {
        return ownerRepository.findAllByOrderByFullNameAsc().stream().map(OwnerService::toResponse).toList();
    }

    @Transactional
    public OwnerResponse update(Long id, OwnerRequest req) {
        Owner o = findOrThrow(id);

        if (req.email() != null && !req.email().equals(o.getEmail()) && ownerRepository.existsByEmail(req.email())) {
            throw new BusinessException("Email уже занят: " + req.email());
        }

        o.setFullName(req.fullName());
        o.setPhone(req.phone());
        o.setEmail(req.email());
        
        return toResponse(o);
    }

    @Transactional 
    public void delete(Long id) {
        Owner o = findOrThrow(id);

        if (!o.getHamsters().isEmpty()) {
            throw new BusinessException("Нельзя удалить хозяина, у которого есть хомяки");
        }

        ownerRepository.delete(o);
    }

    private Owner findOrThrow(Long id) {
        return ownerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Хозяин не найден: id = " + id));
    }

    private static OwnerResponse toResponse(Owner o) {
        return new OwnerResponse(o.getId(), o.getFullName(), o.getPhone(), o.getEmail(), o.getCreatedAt());
    }
}
