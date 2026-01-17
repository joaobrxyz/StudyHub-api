package com.example.studyhub.service;

import com.example.studyhub.model.Usuario;
import com.example.studyhub.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    // Lógica para autenticar o usuário
    public String autenticar(String email, String senha) {

        // 1. Busca o usuário pelo email
        Optional<Usuario> usuarioOptional = repository.findByEmail(email);

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();

            // 2. Compara a senha informada (texto puro) com o hash (do banco)
            if (passwordEncoder.matches(senha, usuario.getSenha())) {

                // 3. Senhas corretas: Gera e retorna o token JWT
                return jwtService.generateToken(usuario.getEmail());
            }
        }

        // Usuário não encontrado ou senha incorreta
        return null;
    }


    public Usuario criar(Usuario usuario) {
        boolean emailEmUso = repository.findByEmail(usuario.getEmail()).isPresent();
        if (emailEmUso) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está em uso.");
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setRole("USER");
        return repository.save(usuario);
    }

    public Usuario buscarPorId(String id) {
        return repository.findById(id).orElse(null);
    }

    // --- Lógica para Solicitar a Recuperação (Gera Token e Envia Email) ---
    public void createPasswordResetToken(String email) {

        Optional<Usuario> usuarioOptional = repository.findByEmail(email);

        if (usuarioOptional.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioOptional.get();

        // Gera Token e Define Expiração (60 minutos)
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(60);

        // Salva no MongoDB
        usuario.setResetToken(token);
        usuario.setTokenExpiryDate(expiryDate);
        repository.save(usuario);

        emailService.sendPasswordResetEmail(usuario.getEmail(), token);
    }

    // --- Lógica para Redefinir a Senha (Valida Token e Atualiza Senha) ---
    public boolean resetPassword(String token, String newPassword) {

        Optional<Usuario> usuarioOptional = repository.findByResetToken(token);

        // 1. Validação do Token e Expiração
        if (usuarioOptional.isEmpty()) {
            return false; // Token não encontrado
        }

        Usuario usuario = usuarioOptional.get();

        if (usuario.getTokenExpiryDate() != null && usuario.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            // Token expirado
            usuario.setResetToken(null);
            usuario.setTokenExpiryDate(null);
            repository.save(usuario);
            return false;
        }

        // 2. Hash da nova senha e atualização
        String encodedPassword = passwordEncoder.encode(newPassword);
        usuario.setSenha(encodedPassword); // Usa setSenha()

        // 3. Invalida o token
        usuario.setResetToken(null);
        usuario.setTokenExpiryDate(null);
        repository.save(usuario); // Atualiza o documento no MongoDB

        return true;
    }
}
