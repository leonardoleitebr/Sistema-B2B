package com.b2b.sistema.controller;

import com.b2b.sistema.dto.UsuarioRequestDTO;
import com.b2b.sistema.dto.UsuarioResponseDTO;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints REST do RF01 - Cadastro e gestao de usuarios e perfis de acesso.
 *
 * A partir da Sprint 2 (RF02), todas as rotas aqui exigem um token valido
 * de um usuario com perfil ROLE_ADMIN - a verificacao e feita pelo
 * AutenticacaoInterceptor antes mesmo de chegar neste controller.
 *
 * @CrossOrigin("*") libera o acesso do frontend (arquivo estatico/porta diferente)
 * para fins didaticos deste projeto academico.
 */
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponseDTO> listar() {
        return usuarioService.listarTodos().stream()
                .map(UsuarioResponseDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO buscar(@PathVariable Long id) {
        return new UsuarioResponseDTO(usuarioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@RequestBody UsuarioRequestDTO dto) {
        Usuario salvo = usuarioService.cadastrar(dto.paraEntidade());
        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioResponseDTO(salvo));
    }

    @PutMapping("/{id}")
    public UsuarioResponseDTO atualizar(@PathVariable Long id, @RequestBody UsuarioRequestDTO dto) {
        Usuario atualizado = usuarioService.atualizar(id, dto.paraEntidade());
        return new UsuarioResponseDTO(atualizado);
    }

    @PatchMapping("/{id}/inativar")
    public UsuarioResponseDTO inativar(@PathVariable Long id) {
        return new UsuarioResponseDTO(usuarioService.inativar(id));
    }

    @PatchMapping("/{id}/ativar")
    public UsuarioResponseDTO ativar(@PathVariable Long id) {
        return new UsuarioResponseDTO(usuarioService.ativar(id));
    }
}

