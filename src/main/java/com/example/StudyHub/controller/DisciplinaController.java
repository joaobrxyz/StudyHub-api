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

import com.example.studyhub.model.Disciplina;
import com.example.studyhub.repository.DisciplinaRepository;

@RestController
@RequestMapping("/disciplinas")
public class DisciplinaController {

    private final DisciplinaRepository disciplinaRepository;

    public DisciplinaController(DisciplinaRepository disciplinaRepository) {
        this.disciplinaRepository = disciplinaRepository;
    }

    // Listar todas as disciplinas
    @GetMapping
    public List<Disciplina> listarTodos() {
        return disciplinaRepository.findAll();
    }

    // Criar nova disciplina
    @PostMapping
    public Disciplina criar(@RequestBody Disciplina disciplina) {
        return disciplinaRepository.save(disciplina);
    }

    // Buscar disciplina por ID
    @GetMapping("/{id}")
    public Disciplina buscarPorId(@PathVariable String id) {
        return disciplinaRepository.findById(id).orElse(null);
    }

    // Deletar disciplina por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarPorId(@PathVariable String id) {
        if (disciplinaRepository.existsById(id)) {
            disciplinaRepository.deleteById(id);
            return ResponseEntity.ok("Disciplina deletada com sucesso!");
        } else {
            return ResponseEntity.status(404).body("Disciplina não encontrada!");
        }
    }

    // Deletar todas as disciplinas
    @DeleteMapping("/todos")
    public ResponseEntity<String> deletarTodos() {
        disciplinaRepository.deleteAll();
        return ResponseEntity.ok("Todas as disciplinas foram deletadas!");
    }
}
