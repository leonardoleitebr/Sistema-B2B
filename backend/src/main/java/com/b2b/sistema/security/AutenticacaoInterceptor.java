package com.b2b.sistema.security;

import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Optional;

/**
 * RF02 - Autenticacao e controle de acesso por perfil.
 *
 * Regra 1: o acesso a cada funcionalidade e validado de acordo com o
 *          perfil do usuario logado.
 * Regra 2: tentativas de acesso a uma funcionalidade nao autorizada sao
 *          bloqueadas.
 *
 * Este interceptor roda antes de qualquer controller em "/api/**"
 * (exceto "/api/auth/**", que precisa ficar liberado para o login
 * acontecer). Ele:
 *   1. Exige um token valido (header "Authorization: Bearer TOKEN").
 *   2. Bloqueia usuarios que foram inativados depois do login.
 *   3. Restringe rotas de administracao (gestao de usuarios e perfis)
 *      somente ao perfil ROLE_ADMIN.
 */
public class AutenticacaoInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public AutenticacaoInterceptor(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        // libera o "preflight" do CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extrairToken(request);
        if (token == null) {
            responderErro(response, 401, "Voce precisa estar logado para acessar este recurso.");
            return false;
        }

        Optional<Long> usuarioIdOpt = tokenService.buscarUsuarioIdPorToken(token);
        if (usuarioIdOpt.isEmpty()) {
            responderErro(response, 401, "Sessao invalida ou expirada. Faca login novamente.");
            return false;
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioIdOpt.get());
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().isAtivo()) {
            responderErro(response, 401, "Usuario inativo ou nao encontrado. Faca login novamente.");
            return false;
        }

        Usuario usuarioLogado = usuarioOpt.get();

        // Regra 1 e 2: somente ROLE_ADMIN pode gerenciar usuarios, perfis, categorias,
        // produtos e tabelas de precos. GET em "/api/configuracoes" fica liberado para
        // qualquer perfil logado (o Cliente precisa saber o pedido minimo vigente);
        // apenas a alteracao (PUT) e exclusiva do Administrador.
        String caminho = request.getRequestURI();
        boolean rotaAdministrativa = caminho.startsWith("/api/usuarios")
                || caminho.startsWith("/api/roles")
                || caminho.startsWith("/api/categorias")
                || caminho.startsWith("/api/produtos")
                || caminho.startsWith("/api/tabelas-precos")
                || (caminho.startsWith("/api/configuracoes") && !"GET".equalsIgnoreCase(request.getMethod()));

        if (rotaAdministrativa && !"ROLE_ADMIN".equals(usuarioLogado.getRole().getNome())) {
            responderErro(response, 403, "Acesso negado: seu perfil nao tem permissao para esta funcionalidade.");
            return false;
        }

        // disponibiliza o usuario logado para os controllers, se precisarem
        request.setAttribute("usuarioLogado", usuarioLogado);
        return true;
    }

    private String extrairToken(HttpServletRequest request) {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            return cabecalho.substring("Bearer ".length());
        }
        return null;
    }

    private void responderErro(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"mensagem\":\"" + mensagem + "\"}");
    }
}
