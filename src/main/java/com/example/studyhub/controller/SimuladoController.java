package com.example.studyhub.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.studyhub.dto.GerarSimuladoDTO;
import com.example.studyhub.model.Usuario;
import com.example.studyhub.service.SimuladoService;
import com.example.studyhub.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.studyhub.model.Simulado;

@RestController
@RequestMapping("/simulados")
public class SimuladoController {
    @Autowired
    private SimuladoService simuladoService;

    @Autowired
    private UsuarioService usuarioService;

    // Listar todos os simulados do usuário
    @GetMapping
    public ResponseEntity<List<Simulado>> listarSimuladosDoUsuario() {

        Usuario usuario = usuarioService.verificarAutenticacao();

        String usuarioId = usuario.getId();

        // Chama o serviço para filtrar
        List<Simulado> simulados = simuladoService.listarPorUsuario(usuarioId);

        return ResponseEntity.ok(simulados);
    }

    // Criar um novo simulado
    @PostMapping
    public ResponseEntity<Map<String, String>> criar(@RequestBody Simulado simulado) {

        Usuario usuario = usuarioService.verificarAutenticacao();

        // Injeta o ID do MongoDB no objeto Simulado
        String usuarioId = usuario.getId();
        simulado.setIdUser(usuarioId);

        // Salva o simulado (o resultado é armazenado, mas não retornado ao cliente)
        simuladoService.criar(simulado);

        // Constrói o JSON de sucesso
        Map<String, String> response = Collections.singletonMap("message", "Simulado criado com sucesso.");

        // Retorna a mensagem com o status 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/gerar")
    public ResponseEntity<Simulado> gerarSimulado(@RequestBody GerarSimuladoDTO dto) {
        // 1. Pega o usuário logado
        Usuario usuario = usuarioService.verificarAutenticacao();

        // 2. Gera o simulado
        Simulado novoSimulado = simuladoService.gerarSimuladoAutomatico(dto, usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(novoSimulado);
    }

    // Buscar simulado por ID
    @GetMapping("/{id}")
    public ResponseEntity<Simulado> buscarPorId(@PathVariable String id) {

        Usuario usuario = usuarioService.verificarAutenticacao();

        String usuarioId = usuario.getId();

        // Chama o serviço para buscar e verificar a propriedade
        Optional<Simulado> simuladoOpt = simuladoService.buscarPorIdEUsuario(id, usuarioId);

        // Retorna o resultado
        if (simuladoOpt.isPresent()) {
            // 200 OK
            return ResponseEntity.ok(simuladoOpt.get());
        } else {
            // Se o simulado não existe OU se o simulado existe mas não pertence ao usuário: 404
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarSimulado(@PathVariable String id, @RequestBody Simulado simuladoAtualizado) {
        usuarioService.verificarAutenticacao();

        simuladoService.atualizar(id, simuladoAtualizado);

        return ResponseEntity.noContent().build();
    }

    // Deletar simulado por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPorId(@PathVariable String id) {

        Usuario usuario = usuarioService.verificarAutenticacao();

        String usuarioId = usuario.getId();

        // Chama o serviço para verificar a propriedade e deletar
        boolean deletado = simuladoService.deletarPorId(id, usuarioId);

        // Retorna a resposta
        if (deletado) {
            // Sucesso na exclusão
            return ResponseEntity.noContent().build(); // 204 No Content é o padrão REST
        } else {
            // Se o simulado não existe OU não pertence ao usuário logado: 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }
}
