package com.b2b.sistema.service;

import com.b2b.sistema.dto.UsuarioRequestDTO;
import com.b2b.sistema.exception.RegraNegocioException;
import com.b2b.sistema.model.TabelaPreco;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.repository.TabelaPrecoRepository;
import com.b2b.sistema.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Regras de negocio do RF01 - Cadastro e gestao de usuarios e perfis de acesso.
 *
 * A partir da evolucao do catalogo/pedidos (RF06/RF10), tambem resolve os vinculos
 * opcionais de um usuario Cliente (tabela de precos, vendedor responsavel) e do
 * limite de desconto de um usuario Vendedor.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TabelaPrecoRepository tabelaPrecoRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository, TabelaPrecoRepository tabelaPrecoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tabelaPrecoRepository = tabelaPrecoRepository;
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
    public Usuario cadastrar(UsuarioRequestDTO dto) {
        Usuario usuario = dto.paraEntidade();

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new RegraNegocioException("O e-mail e obrigatorio.");
        }
        if (usuario.getRole() == null) {
            throw new RegraNegocioException("O perfil de acesso e obrigatorio.");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RegraNegocioException("Ja existe um usuario cadastrado com este e-mail.");
        }

        aplicarVinculosOpcionais(usuario, dto);

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setAtivo(false);
        return usuarioRepository.save(usuario);
    }

    public Usuario atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = buscarPorId(id);
        Usuario dadosAtualizados = dto.paraEntidade();

        boolean mudouEmail = !usuario.getEmail().equalsIgnoreCase(dadosAtualizados.getEmail());
        if (mudouEmail && usuarioRepository.existsByEmail(dadosAtualizados.getEmail())) {
            throw new RegraNegocioException("Ja existe um usuario cadastrado com este e-mail.");
        }

        usuario.setNome(dadosAtualizados.getNome());
        usuario.setEmail(dadosAtualizados.getEmail());
        usuario.setRole(dadosAtualizados.getRole());

        aplicarVinculosOpcionais(usuario, dto);

        // Senha so e alterada se o campo vier preenchido
        if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dadosAtualizados.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }

    /**
     * RF06/RF10 - resolve os vinculos opcionais enviados pelo formulario de usuario:
     * tabela de precos e vendedor responsavel (uso esperado: perfil Cliente) e limite
     * de desconto (uso esperado: perfil Vendedor). Campos nao enviados (null) limpam o vinculo.
     */
    private void aplicarVinculosOpcionais(Usuario usuario, UsuarioRequestDTO dto) {
        if (dto.getTabelaPrecoId() != null) {
            TabelaPreco tabelaPreco = tabelaPrecoRepository.findById(dto.getTabelaPrecoId())
                    .orElseThrow(() -> new RegraNegocioException("Tabela de precos nao encontrada."));
            usuario.setTabelaPreco(tabelaPreco);
        } else {
            usuario.setTabelaPreco(null);
        }

        if (dto.getVendedorResponsavelId() != null) {
            Usuario vendedor = usuarioRepository.findById(dto.getVendedorResponsavelId())
                    .orElseThrow(() -> new RegraNegocioException("Vendedor responsavel nao encontrado."));
            if (vendedor.getRole() == null || !"ROLE_VENDEDOR".equals(vendedor.getRole().getNome())) {
                throw new RegraNegocioException("O vendedor responsavel precisa ter o perfil Vendedor.");
            }
            usuario.setVendedorResponsavel(vendedor);
        } else {
            usuario.setVendedorResponsavel(null);
        }

        BigDecimal limite = dto.getLimiteDescontoPercentual();
        if (limite != null) {
            if (limite.compareTo(BigDecimal.ZERO) < 0 || limite.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new RegraNegocioException("O limite de desconto deve estar entre 0 e 100.");
            }
            usuario.setLimiteDescontoPercentual(limite);
        } else {
            usuario.setLimiteDescontoPercentual(BigDecimal.ZERO);
        }
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

    /** RF10 - lista os clientes atendidos por um vendedor. */
    public List<Usuario> listarClientesDoVendedor(Long vendedorId) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "ROLE_CLIENTE".equals(u.getRole().getNome()))
                .filter(u -> u.getVendedorResponsavel() != null && u.getVendedorResponsavel().getId().equals(vendedorId))
                .toList();
    }
}
