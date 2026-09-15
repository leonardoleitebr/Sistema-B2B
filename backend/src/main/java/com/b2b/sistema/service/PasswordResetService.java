package com.b2b.sistema.service;

import com.b2b.sistema.exception.RegraNegocioException;
import com.b2b.sistema.model.PasswordResetToken;
import com.b2b.sistema.model.Usuario;
import com.b2b.sistema.repository.PasswordResetTokenRepository;
import com.b2b.sistema.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fluxo de "esqueci minha senha" (redefinicao de senha por e-mail),
 * complementar ao RF01/RF02.
 *
 * Fluxo completo, do ponto de vista do usuario:
 *   1. O Administrador cadastra o usuario e escolhe o perfil de acesso
 *      (RF01) e depois o ativa.
 *   2. Na tela de login, o usuario clica em "Esqueci minha senha" e
 *      informa o seu e-mail.
 *   3. Recebe por e-mail um codigo de redefinicao, valido por um tempo
 *      curto.
 *   4. Informa esse codigo e a nova senha (escolhida por ele mesmo, do
 *      jeito que preferir) na mesma tela, concluindo a redefinicao.
 */
@Service
public class PasswordResetService {

    /** Tempo de validade do codigo de redefinicao, em minutos. */
    private static final int VALIDADE_EM_MINUTOS = 30;

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public PasswordResetService(UsuarioRepository usuarioRepository,
                                 PasswordResetTokenRepository tokenRepository,
                                 EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    /**
     * Gera um codigo de redefinicao e envia por e-mail, caso o e-mail
     * informado pertenca a um usuario ativo.
     *
     * Por seguranca, quem chama este metodo deve sempre devolver a mesma
     * mensagem de sucesso ao frontend, exista ou nao o e-mail no banco -
     * assim ninguem descobre, por tentativa e erro, quais e-mails estao
     * cadastrados no sistema.
     */
    public void solicitarRedefinicao(String email) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            // Usuario inativo nao deveria conseguir voltar a acessar o
            // sistema sem passar pelo Administrador de novo.
            if (!usuario.isAtivo()) {
                return;
            }

            String codigo = gerarCodigo();
            LocalDateTime expiracao = LocalDateTime.now().plusMinutes(VALIDADE_EM_MINUTOS);
            tokenRepository.save(new PasswordResetToken(codigo, usuario, expiracao));

            String corpo = "Ola, " + usuario.getNome() + "!\n\n"
                    + "Recebemos uma solicitacao para redefinir a sua senha no Sistema B2B.\n"
                    + "Use o codigo abaixo na tela de redefinicao de senha:\n\n"
                    + codigo + "\n\n"
                    + "Esse codigo e valido por " + VALIDADE_EM_MINUTOS + " minutos.\n"
                    + "Se voce nao solicitou isso, apenas ignore este e-mail.";

            try {
                emailService.enviarEmailSimples(usuario.getEmail(), "Redefinicao de senha - Sistema B2B", corpo);
            } catch (Exception e) {
                // Nao deixamos o erro de envio (ex.: configuracao de SMTP)
                // virar um 500 cru para o frontend; convertemos numa
                // mensagem de negocio tratavel pelo GlobalExceptionHandler.
                throw new RegraNegocioException(
                        "Nao foi possivel enviar o e-mail de redefinicao no momento. Tente novamente mais tarde.");
            }
        });
    }

    /**
     * Valida o codigo recebido por e-mail e define a nova senha (escolhida
     * pelo proprio usuario) para a conta correspondente.
     */
    public void redefinirSenha(String token, String novaSenha) {
        if (novaSenha == null || novaSenha.isBlank()) {
            throw new RegraNegocioException("A nova senha e obrigatoria.");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RegraNegocioException("Codigo invalido ou ja utilizado."));

        if (resetToken.isUsado()) {
            throw new RegraNegocioException("Codigo invalido ou ja utilizado.");
        }
        if (resetToken.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("Codigo expirado. Solicite um novo codigo.");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        tokenRepository.save(resetToken);
    }

    private String gerarCodigo() {
        // Codigo curto (8 caracteres), mais facil de copiar do e-mail do
        // que um UUID inteiro, mas ainda dificil de adivinhar.
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
