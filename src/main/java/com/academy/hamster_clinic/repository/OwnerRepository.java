package com.academy.hamster_clinic.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.academy.hamster_clinic.entity.Owner;
import java.util.List;
import java.util.Optional;


public interface OwnerRepository extends JpaRepository<Owner, Long> {
    
    Optional<Owner> findByEmain(String email);
    boolean existsByEmail(String email);
    List<Owner> findAllByOrderByFullNameAsc();
}
