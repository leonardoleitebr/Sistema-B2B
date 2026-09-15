package com.b2b.sistema.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RF02 - Autenticacao e controle de acesso por perfil.
 *
 * Controla os tokens de sessao dos usuarios logados. Para manter a sprint
 * simples de explicar, os tokens sao guardados em memoria (um Map), em vez
 * de usar JWT ou uma tabela de sessoes no banco. Isso significa que, se o
 * backend for reiniciado, todo mundo precisa logar de novo - o que e
 * perfeitamente aceitavel para fins didaticos.
 */
@Component
public class TokenService {

    // token (String) -> id do usuario logado
    private final Map<String, Long> tokensAtivos = new ConcurrentHashMap<>();

    public String gerarToken(Long usuarioId) {
        String token = UUID.randomUUID().toString();
        tokensAtivos.put(token, usuarioId);
        return token;
    }

    public Optional<Long> buscarUsuarioIdPorToken(String token) {
        return Optional.ofNullable(tokensAtivos.get(token));
    }

    public void invalidarToken(String token) {
        tokensAtivos.remove(token);
    }
}
