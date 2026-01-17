package com.example.studyhub.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // O Spring injeta automaticamente a instância configurada do JavaMailSender
    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envia o e-mail de recuperação de senha.
     * @param to O endereço de e-mail do usuário
     * @param token O token de recuperação gerado
     */
    public void sendPasswordResetEmail(String to, String token) {

        // 1. Monta o link completo de recuperação para o Front-end
        String resetUrl = "https://studyhub.com.br/auth/reset-password?token=" + token;

        // 2. Cria a mensagem de e-mail
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("Study Hub <noreply@studyhub.com.br>");
        message.setTo(to);
        message.setSubject("Study Hub: Link de Recuperação de Senha");

        String emailContent = String.format(
                "Olá,\n\n" +
                        "Você solicitou a redefinição de sua senha. Por favor, clique no link abaixo para criar uma nova senha. " +
                        "Este link irá expirar em 60 minutos:\n\n" +
                        "%s\n\n" +
                        "Se você não solicitou isso, ignore este e-mail.\n\n" +
                        "Atenciosamente,\n" +
                        "Equipe Study Hub",
                resetUrl
        );

        message.setText(emailContent);

        // 3. Envia o e-mail
        mailSender.send(message);
    }
}