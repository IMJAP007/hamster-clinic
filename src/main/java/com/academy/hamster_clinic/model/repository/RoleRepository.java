package com.academy.hamster_clinic.model.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.academy.hamster_clinic.model.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}