package com.academy.hamster_clinic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.academy.hamster_clinic.entity.Hamster;

public interface HamsterRepository extends JpaRepository<Hamster, Long> {
    
    List<Hamster> findByOwnerId(Long ownerId);
    List<Hamster> findByNameContainingIgnoreCase(String fragment);
    boolean existsByOwnerIdAndName(Long ownerId, String name);
}
