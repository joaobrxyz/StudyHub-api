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

import com.example.studyhub.model.Exame;
import com.example.studyhub.repository.ExameRepository;

@RestController
@RequestMapping("/exames")
public class ExameController {

    private final ExameRepository exameRepository;

    public ExameController(ExameRepository exameRepository) {
        this.exameRepository = exameRepository;
    }

    // Listar todos os exames
    @GetMapping
    public List<Exame> listarTodos() {
        return exameRepository.findAll();
    }

    // Criar um novo exame
    @PostMapping
    public Exame criar(@RequestBody Exame exame) {
        return exameRepository.save(exame);
    }

    // Buscar exame por ID
    @GetMapping("/{id}")
    public Exame buscarPorId(@PathVariable String id) {
        return exameRepository.findById(id).orElse(null);
    }

    // Deletar exame por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarPorId(@PathVariable String id) {
        if (exameRepository.existsById(id)) {
            exameRepository.deleteById(id);
            return ResponseEntity.ok("Exame deletado com sucesso!");
        } else {
            return ResponseEntity.status(404).body("Exame não encontrado!");
        }
    }

    // Deletar todos os exames
    @DeleteMapping("/todos")
    public ResponseEntity<String> deletarTodos() {
        exameRepository.deleteAll();
        return ResponseEntity.ok("Todos os exames foram deletados!");
    }
}
