package com.b2b.sistema.controller;

import com.b2b.sistema.dto.EsqueciSenhaRequestDTO;
import com.b2b.sistema.dto.RedefinirSenhaRequestDTO;
import com.b2b.sistema.service.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoints do fluxo de "esqueci minha senha".
 *
 * Ficam sob "/api/auth", junto do login - ou seja, ja saem liberados do
 * AutenticacaoInterceptor (ver WebConfig), porque o usuario ainda nao
 * esta logado quando precisa redefinir a propria senha.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<Map<String, String>> esqueciSenha(@RequestBody EsqueciSenhaRequestDTO dto) {
        passwordResetService.solicitarRedefinicao(dto.getEmail());

        // Mensagem generica de proposito: nao revela se o e-mail existe
        // ou nao no banco (evita que alguem descubra e-mails cadastrados
        // por tentativa e erro).
        return ResponseEntity.ok(Map.of(
                "mensagem", "Se o e-mail informado estiver cadastrado e ativo, enviamos um codigo de redefinicao."
        ));
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@RequestBody RedefinirSenhaRequestDTO dto) {
        passwordResetService.redefinirSenha(dto.getToken(), dto.getNovaSenha());
        return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso! Faca login com a nova senha."));
    }
}
