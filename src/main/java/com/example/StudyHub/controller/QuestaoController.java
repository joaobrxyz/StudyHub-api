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

import com.example.studyhub.model.Questao;
import com.example.studyhub.repository.QuestaoRepository;

@RestController
@RequestMapping("/questoes")
public class QuestaoController {

    private final QuestaoRepository questaoRepository;

    public QuestaoController(QuestaoRepository questaoRepository) {
        this.questaoRepository = questaoRepository;
    }

    // Listar todas as questões
    @GetMapping
    public List<Questao> listarTodos() {
        return questaoRepository.findAll();
    }

    // Criar nova questão
    @PostMapping
    public Questao criar(@RequestBody Questao questao) {
        return questaoRepository.save(questao);
    }

    // Buscar questão por ID
    @GetMapping("/{id}")
    public Questao buscarPorId(@PathVariable String id) {
        return questaoRepository.findById(id).orElse(null);
    }

    // Deletar questão por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarPorId(@PathVariable String id) {
        if (questaoRepository.existsById(id)) {
            questaoRepository.deleteById(id);
            return ResponseEntity.ok("Questão deletada com sucesso!");
        } else {
            return ResponseEntity.status(404).body("Questão não encontrada!");
        }
    }

    // Deletar todas as questões
    @DeleteMapping("/todos")
    public ResponseEntity<String> deletarTodos() {
        questaoRepository.deleteAll();
        return ResponseEntity.ok("Todas as questões foram deletadas!");
    }
}
