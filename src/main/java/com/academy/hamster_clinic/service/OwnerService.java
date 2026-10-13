package com.academy.hamster_clinic.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Owner create(Owner owner){
        if (owner.getEmail() != null && ownerRepository.existsByEmail(owner.getEmail())) {
            throw new BusinessException("Хозяин с таким email уже существует: " + owner.getEmail());
        }

        return ownerRepository.save(owner);
    }

    public Owner getById(Long id) {
        return ownerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Хозяин не найден: id = " + id));
    }

    public List<Owner> list() {
        return ownerRepository.findAllByOrderByFullNameAsc();
    }

    @Transactional
    public Owner update(Long id, String fullName, String phone, String email) {
        Owner owner = getById(id);
        owner.setFullName(fullName);
        owner.setPhone(phone);
        owner.setEmail(email);
        
        return owner;
    }

    @Transactional 
    public void delete(Long id) {
        Owner owner = getById(id);

        if (!owner.getHamsters().isEmpty()) {
            throw new BusinessException("Нельзя удалить хозяина, у которого есть хомяки");
        }

        ownerRepository.delete(owner);
    }
}
