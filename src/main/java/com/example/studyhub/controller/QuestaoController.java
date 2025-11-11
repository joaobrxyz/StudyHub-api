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
                                                         @RequestParam(required = false) String disciplina,
                                                         @RequestParam(required = false) String dificuldade,
                                                         @RequestParam(required = false) String instituicao,
                                                         @RequestParam(required = false) String ano,
                                                         @RequestParam(required = false) String termo,

                                                         @PageableDefault(size = 20, sort = {"enunciado"}) Pageable paginacao
    ) {
        Dificuldade dificuldadeEnum = null;

        // Lógica de Conversão e UPPERCASE
        if (dificuldade != null && !dificuldade.trim().isEmpty()) {
            try {
                // Converte a string para MAIÚSCULO e depois para o Enum
                dificuldadeEnum = Dificuldade.valueOf(dificuldade.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Dificuldade inválida: " + dificuldade);
            }
        }

        // 2. Chama o service com o objeto Pageable
        Page<Questao> resultado = questaoService.buscarFiltrado(
                disciplina,
                dificuldadeEnum,
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
