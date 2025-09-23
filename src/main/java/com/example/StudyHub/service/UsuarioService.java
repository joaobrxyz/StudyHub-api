package com.example.studyhub.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.studyhub.model.Usuario;
import com.example.studyhub.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    // Buscar todos os usuários
    public List<Usuario> listarTodos() {
        return repository.findAll();
    }

    // Buscar usuário por e-mail
    public Usuario buscarPorEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    // Salvar novo usuário
    public Usuario salvar(Usuario usuario) {
        return repository.save(usuario);
    }

    // Deletar usuário por id
    public void deletar(String id) {
        repository.deleteById(id);
    }
}
