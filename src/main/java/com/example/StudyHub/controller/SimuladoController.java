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

import com.example.studyhub.model.Simulado;
import com.example.studyhub.repository.SimuladoRepository;

@RestController
@RequestMapping("/simulados")
public class SimuladoController {

    private final SimuladoRepository simuladoRepository;

    public SimuladoController(SimuladoRepository simuladoRepository) {
        this.simuladoRepository = simuladoRepository;
    }

    // Listar todos os simulados
    @GetMapping
    public List<Simulado> listarTodos() {
        return simuladoRepository.findAll();
    }

    // Criar um novo simulado
    @PostMapping
    public Simulado criar(@RequestBody Simulado simulado) {
        return simuladoRepository.save(simulado);
    }

    // Buscar simulado por ID
    @GetMapping("/{id}")
    public Simulado buscarPorId(@PathVariable String id) {
        return simuladoRepository.findById(id).orElse(null);
    }

    // Deletar simulado por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarPorId(@PathVariable String id) {
        if (simuladoRepository.existsById(id)) {
            simuladoRepository.deleteById(id);
            return ResponseEntity.ok("Simulado deletado com sucesso!");
        } else {
            return ResponseEntity.status(404).body("Simulado não encontrado!");
        }
    }

    // Deletar todos os simulados
    @DeleteMapping("/todos")
    public ResponseEntity<String> deletarTodos() {
        simuladoRepository.deleteAll();
        return ResponseEntity.ok("Todos os simulados foram deletados!");
    }
}
