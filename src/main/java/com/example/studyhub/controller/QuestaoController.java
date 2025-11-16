package com.example.studyhub.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.example.studyhub.model.Dificuldade;
import com.example.studyhub.service.QuestaoService;
import com.example.studyhub.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.studyhub.model.Questao;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/questoes")
public class QuestaoController {
    @Autowired
    private QuestaoService questaoService;

    @Autowired
    private UsuarioService usuarioService;

    // Criar nova questão
    @PostMapping
    public ResponseEntity<Map<String, String>> criarQuestoes(@Valid @RequestBody List<Questao> questoes) {

        // Uma linha verifica as duas permissões (401 e 403)
        usuarioService.verificarPermissaoAdmin();

        // Se o código chegou aqui, o usuário é autenticado e é ADMIN.

        List<Questao> questoesSalvas = questaoService.criarQuestoes(questoes);
        int quantidade = questoesSalvas.size();
        String mensagem = "Questões adicionadas: " + quantidade;

        Map<String, String> response = Collections.singletonMap("message", mensagem);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<Questao>> buscarFiltrado(
            @RequestParam(required = false) List<String> disciplina,     // LISTA
            @RequestParam(required = false) List<String> dificuldade,    // LISTA
            @RequestParam(required = false) List<String> instituicao,    // LISTA
            @RequestParam(required = false) List<String> ano,            // LISTA
            @RequestParam(required = false) String termo,                 // STRING (Busca Parcial no Enunciado)

            @PageableDefault(size = 20, sort = {"enunciado"}) Pageable paginacao
    ) {
        // --- Conversão de Dificuldade para Enum ---
        List<Dificuldade> dificuldadesEnum = null;
        if (dificuldade != null && !dificuldade.isEmpty()) {
            dificuldadesEnum = dificuldade.stream()
                    .map(d -> {
                        try {
                            return Dificuldade.valueOf(d.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            System.err.println("Dificuldade inválida: " + d);
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toList());
        }

        // 2. Chama o service com as Listas
        Page<Questao> resultado = questaoService.buscarFiltrado(
                disciplina,
                dificuldadesEnum,
                instituicao,
                ano,
                termo,
                paginacao
        );
        return ResponseEntity.ok(resultado);
    }

    // Buscar questão por ID
    @GetMapping("/{id}")
    public Questao buscarPorId(@PathVariable String id) {
        var questao = questaoService.buscarPorId(id);
        if (questao != null) {
            return questao;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada.");
        }
    }

    // Deletar questão por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarPorId(@PathVariable String id) {
        // Uma linha verifica as duas permissões (401 e 403)
        usuarioService.verificarPermissaoAdmin();

        // Se o código chegou aqui, o usuário é autenticado e é ADMIN.
        return questaoService.deletarPorId(id);
    }

}
