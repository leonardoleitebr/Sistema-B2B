package com.b2b.sistema.controller;

import com.b2b.sistema.model.Role;
import com.b2b.sistema.repository.RoleRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoint de apoio ao RF01: expoe os perfis de acesso (Role) cadastrados
 * no banco, para que o frontend monte a lista de selecao ao cadastrar
 * ou editar um usuario.
 */
@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public List<Role> listar() {
        return roleRepository.findAll();
    }
}
