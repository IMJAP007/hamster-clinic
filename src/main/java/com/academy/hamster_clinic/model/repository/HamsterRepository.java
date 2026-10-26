package com.academy.hamster_clinic.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.academy.hamster_clinic.model.entity.Hamster;

public interface HamsterRepository extends JpaRepository<Hamster, Long> {
    
    List<Hamster> findByOwnerId(Long ownerId);
    List<Hamster> findByNameContainingIgnoreCase(String fragment);
    
    @EntityGraph(attributePaths = "owner")
    List<Hamster> findByOwnerFullNameContainingIgnoreCase(String fullName);

    @Override
    @EntityGraph(attributePaths = "owner")
    List<Hamster> findAll();
    
    boolean existsByOwnerIdAndName(Long ownerId, String name);
}
