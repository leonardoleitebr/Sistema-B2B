package com.b2b.sistema.service;

import com.b2b.sistema.exception.RegraNegocioException;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Regras de negocio do RF01 - Cadastro e gestao de usuarios e perfis de acesso.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Usuario nao encontrado."));
    }

    /**
     * Regra 1: cada usuario deve possuir um perfil definido (validado pelo enum).
     * Regra 2: nao permite cadastrar dois usuarios com o mesmo e-mail.
     */
    public Usuario cadastrar(Usuario usuario) {
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new RegraNegocioException("O e-mail e obrigatorio.");
        }
        if (usuario.getRole() == null) {
            throw new RegraNegocioException("O perfil de acesso e obrigatorio.");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RegraNegocioException("Ja existe um usuario cadastrado com este e-mail.");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setAtivo(false);
        return usuarioRepository.save(usuario);
    }

    public Usuario atualizar(Long id, Usuario dadosAtualizados) {
        Usuario usuario = buscarPorId(id);

        boolean mudouEmail = !usuario.getEmail().equalsIgnoreCase(dadosAtualizados.getEmail());
        if (mudouEmail && usuarioRepository.existsByEmail(dadosAtualizados.getEmail())) {
            throw new RegraNegocioException("Ja existe um usuario cadastrado com este e-mail.");
        }

        usuario.setNome(dadosAtualizados.getNome());
        usuario.setEmail(dadosAtualizados.getEmail());
        usuario.setRole(dadosAtualizados.getRole());

        // Senha so e alterada se o campo vier preenchido
        if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dadosAtualizados.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario inativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(false);
        return usuarioRepository.save(usuario);
    }

    public Usuario ativar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    /**
     * Regra 3 do RF01 e Regra 3 do RF02: usuario inativado nao pode acessar
     * o sistema, e a senha e comparada com o hash (nunca em texto puro).
     * Usado pelo AuthController (RF02) para emitir o token de sessao.
     */
    public Usuario autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RegraNegocioException("E-mail ou senha invalidos."));

        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Usuario inativo. Acesso negado.");
        }

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new RegraNegocioException("E-mail ou senha invalidos.");
        }

        return usuario;
    }
}
