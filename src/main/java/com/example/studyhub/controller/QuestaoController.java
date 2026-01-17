package com.example.studyhub.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.example.studyhub.model.Dificuldade;
import com.example.studyhub.service.QuestaoService;
import com.example.studyhub.service.UsuarioService;
import com.fasterxml.jackson.databind.JsonMappingException;
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

            // Filtros premium
            @RequestParam(required = false, defaultValue = "false") Boolean apenasErros,
            @RequestParam(required = false, defaultValue = "false") Boolean comResolucao,
            @RequestParam(required = false, defaultValue = "false") Boolean comVideo,

            @PageableDefault(size = 20, sort = {"enunciado"}) Pageable paginacao
    ) {
        // Verificar se algum filtro premium foi acionado
        boolean tentandoUsarPremium = apenasErros || comResolucao || comVideo;

        String usuarioId = null;

        if (tentandoUsarPremium) {
            // Valida se o usuário está logado e se é PREMIUM
            // Se não for, o próprio service de usuário pode lançar um 403 Forbidden
            var usuarioLogado = usuarioService.verificarAcessoPremium();
            usuarioId = usuarioLogado.getId();
        }

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
                apenasErros,
                comResolucao,
                comVideo,
                usuarioId,
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

    // Atualizar parcialmente uma questão por ID
    @PatchMapping("/{id}")
    public ResponseEntity<Questao> patchQuestao(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        // 1. Verificar permissão de ADMIN
        usuarioService.verificarPermissaoAdmin();

        // 2. Chamar o serviço para aplicar as atualizações
        // As exceções (NOT_FOUND, BAD_REQUEST) lançadas no Service serão
        // automaticamente tratadas pelo Spring e retornarão o status HTTP correto.
        Questao questaoAtualizada = questaoService.atualizarParcial(id, updates);
        return ResponseEntity.ok(questaoAtualizada);
    }

    @PatchMapping
    public ResponseEntity<Void> patchQuestoesEmLote(@RequestBody List<Map<String, Object>> listaUpdates) {
        // 1. Verificar permissão (igual ao seu)
        usuarioService.verificarPermissaoAdmin();

        // 2. Chamar o serviço de lote
        questaoService.atualizarEmLote(listaUpdates);

        return ResponseEntity.ok().build(); // Retorna 200 OK sem corpo
    }

    @GetMapping("/lista")
    public ResponseEntity<List<Questao>> buscarPorListaIds(@RequestParam List<String> ids) {
        List<Questao> questoes = questaoService.buscarPorListaIds(ids);
        return ResponseEntity.ok(questoes);
    }

}
