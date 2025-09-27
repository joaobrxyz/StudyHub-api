package com.example.studyhub.service;

import com.example.studyhub.model.Usuario;
import com.example.studyhub.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

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
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setRole("USER");
        return repository.save(usuario);
    }

    public Usuario buscarPorId(String id) {
        return repository.findById(id).orElse(null);
    }
}
