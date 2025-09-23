package com.example.studyhub.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.studyhub.model.Usuario;
import com.example.studyhub.repository.UsuarioRepository;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // Listar todos os usuários
    @GetMapping
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // Criar um novo usuário
    @PostMapping
    public Usuario criar(@RequestBody Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Buscar usuário por ID
    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable String id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    // Deletar usuário por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarPorId(@PathVariable String id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return ResponseEntity.ok("Usuário deletado com sucesso!");
        } else {
            return ResponseEntity.status(404).body("Usuário não encontrado!");
        }
    }

    // Deletar todos os usuários
    @DeleteMapping("/todos")
    public ResponseEntity<String> deletarTodos() {
        usuarioRepository.deleteAll();
        return ResponseEntity.ok("Todos os usuários foram deletados!");
    }
}
