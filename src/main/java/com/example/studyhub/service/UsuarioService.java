package com.example.studyhub.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.studyhub.model.Usuario;
import com.example.studyhub.repository.UsuarioRepository;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    public Usuario verificarAutenticacao() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // **1. CHECAGEM DE AUTENTICAÇÃO (401)**
        // Se o token estiver faltando ou for anônimo, lança 401.
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acesso não autorizado. É necessário estar logado.");
        }

        // 2. BUSCA O USUÁRIO NO BANCO DE DADOS
        String usuarioEmail = authentication.getName();

        // Declara a variável Usuario fora do bloco IF para que possa ser retornada.
        Optional<Usuario> usuarioOpt = buscarPorEmail(usuarioEmail);

        // 3. VERIFICA SE O USUÁRIO EXISTE (401/404)
        if (usuarioOpt.isEmpty()){
            // Se o token é válido, mas o usuário não está no DB (foi deletado),
            // isso indica uma sessão inválida.
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida. Usuário não encontrado.");
        }

        // Retorna o usuário encontrado
        return usuarioOpt.get();
    }

    public Usuario verificarPermissaoAdmin() {
        Usuario usuario = verificarAutenticacao();

        // **2. CHECAGEM DE AUTORIZAÇÃO (403)**
        if (!usuario.getRole().equalsIgnoreCase("ADMIN")) {
            // Lança uma exceção que o Spring converte para 403
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso negado. Somente administradores podem adicionar questões.");
        }

        return usuario; // Retorna o objeto Usuario
    }

    // Buscar usuário por e-mail
    public Optional<Usuario> buscarPorEmail(String email) {
        return repository.findByEmail(email);
    }

    public void deletarUsuario(String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            // Se o ID não for encontrado, lança 404
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }
    }
}
