package com.shopit.project.repository;

import com.shopit.project.model.RoleType;
import com.shopit.project.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(RoleType roleType);
}
