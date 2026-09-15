package com.b2b.sistema.repository;

import com.b2b.sistema.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    // Método útil para buscar uma Role pelo nome (ex: "ROLE_ADMIN")
    Optional<Role> findByNome(String nome);
}