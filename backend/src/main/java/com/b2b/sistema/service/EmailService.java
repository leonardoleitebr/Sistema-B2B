package com.b2b.sistema.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servico de envio de e-mail (modelo fornecido pelo professor), usado
 * pelo fluxo de "esqueci minha senha" (ver PasswordResetService).
 *
 * O remetente e lido de application.properties (spring.mail.username)
 * em vez de ficar fixo no codigo, para nao repetir o mesmo e-mail em
 * dois lugares diferentes.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailSimples(String para, String assunto, String corpo) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(para);
        mensagem.setSubject(assunto);
        mensagem.setText(corpo);
        mailSender.send(mensagem);
    }
}
