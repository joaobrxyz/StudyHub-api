package com.example.studyhub.controller;

import com.example.studyhub.dto.EmailRequest;
import com.example.studyhub.dto.LoginDTO;
import com.example.studyhub.dto.ResetPasswordRequest;
import com.example.studyhub.dto.TokenResponseDTO;
import com.example.studyhub.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.studyhub.model.Usuario;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService service;

    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        // O serviço retorna o token (String) ou null/lança exceção
        String token = service.autenticar(loginDTO.email(), loginDTO.senha());

        if (token != null) {
            // Login bem-sucedido: retorna o token no DTO
            return ResponseEntity.ok(new TokenResponseDTO(token, "Bearer"));
        } else {
            // Login falhou: retorna 401 Unauthorized
            Map<String, String> errorResponse = Collections.singletonMap("error", "E-mail ou senha incorretos.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    // Criar um novo usuário
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> criar(@RequestBody Usuario usuario) {
        // 1. Chama o serviço para salvar o usuário (com criptografia)
        service.criar(usuario);

        // 2. Cria o JSON de resposta
        Map<String, String> response = Collections.singletonMap("message", "Cadastro realizado com sucesso.");

        // 3. Retorna a resposta com o status HTTP 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> requestPasswordReset(@RequestBody EmailRequest emailRequest) {
        service.createPasswordResetToken(emailRequest.email());
        return ResponseEntity.ok("Se o e-mail estiver cadastrado, você receberá um link de redefinição.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {

        if (request.newPassword() == null || request.newPassword().length() < 8) {
            return ResponseEntity.badRequest().body("A nova senha deve ter pelo menos 8 caracteres.");
        }
        boolean success = service.resetPassword(request.token(), request.newPassword());

        if (success) {
            return ResponseEntity.ok("Senha redefinida com sucesso! Você já pode fazer login.");
        } else {
            return ResponseEntity.badRequest().body("Link de redefinição inválido ou expirado. Por favor, solicite um novo.");
        }
    }

}
