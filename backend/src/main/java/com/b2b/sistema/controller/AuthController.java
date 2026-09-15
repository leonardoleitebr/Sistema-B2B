package com.b2b.sistema.controller;

import com.b2b.sistema.dto.LoginRequestDTO;
import com.b2b.sistema.dto.LoginResponseDTO;
import com.b2b.sistema.dto.UsuarioResponseDTO;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.repository.UsuarioRepository;
import com.b2b.sistema.security.TokenService;
import com.b2b.sistema.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * RF02 - Autenticacao e controle de acesso por perfil.
 *
 * Endpoint publico (nao passa pelo AutenticacaoInterceptor): e aqui que
 * o usuario troca e-mail/senha por um token de sessao.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;

    public AuthController(UsuarioService usuarioService, UsuarioRepository usuarioRepository,
                           TokenService tokenService) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO dto) {
        // usuarioService.autenticar ja valida e-mail/senha e se o usuario esta ativo
        Usuario usuario = usuarioService.autenticar(dto.getEmail(), dto.getSenha());
        String token = tokenService.gerarToken(usuario.getId());
        return new LoginResponseDTO(token, usuario);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = extrairToken(request);
        if (token != null) {
            tokenService.invalidarToken(token);
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Usado pelo frontend para restaurar a sessao ao recarregar a pagina
     * (o token fica salvo no navegador; aqui confirmamos se ele ainda e valido
     * e devolvemos os dados atualizados do usuario).
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest request) {
        String token = extrairToken(request);
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("mensagem", "Nao autenticado."));
        }

        Optional<Long> usuarioId = tokenService.buscarUsuarioIdPorToken(token);
        if (usuarioId.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("mensagem", "Sessao invalida ou expirada."));
        }

        return usuarioRepository.findById(usuarioId.get())
                .filter(Usuario::isAtivo)
                .<ResponseEntity<?>>map(usuario -> ResponseEntity.ok(new UsuarioResponseDTO(usuario)))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("mensagem", "Usuario inativo ou nao encontrado.")));
    }

    private String extrairToken(HttpServletRequest request) {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            return cabecalho.substring("Bearer ".length());
        }
        return null;
    }
}
