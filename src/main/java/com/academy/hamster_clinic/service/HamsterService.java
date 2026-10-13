package com.academy.hamster_clinic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Hamster create(Long ownerId, String name) {
        Owner owner = ownerRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Хозяин не найден: id = " + ownerId));

        Hamster hamster = new Hamster();
        hamster.setName(name);
        hamster.setOwner(owner);
        
        return hamsterRepository.save(hamster);
    }

    public Hamster getById(Long id) {
        return hamsterRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Хомяк не найден: id = " + id));
    }

    public List<Hamster> listByOwner(Long ownerId) {
        return hamsterRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public Hamster rename(Long id, String newName) {
        Hamster hamster = getById(id);
        hamster.setName(newName);
        
        return hamster;
    }

    @Transactional
    public void delete(Long id) {
        if (!hamsterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Хомяк не найден: id = " + id);
        }
        
        hamsterRepository.deleteById(id);
    }
}