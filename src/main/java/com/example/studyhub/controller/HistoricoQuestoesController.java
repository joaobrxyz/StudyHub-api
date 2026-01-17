package com.example.studyhub.controller;

import com.example.studyhub.dto.DesempenhoMateriaDTO;
import com.example.studyhub.dto.EstatisticasGeraisDTO;
import com.example.studyhub.dto.RegistrarTentativaDTO;
import com.example.studyhub.model.Usuario;
import com.example.studyhub.model.VolumeEstudo;
import com.example.studyhub.service.HistoricoQuestoesService;
import com.example.studyhub.service.UsuarioService; // Importando seu serviço
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("historico-questoes")
public class HistoricoQuestoesController {

    @Autowired
    private HistoricoQuestoesService historicoService;

    // 1. Injetamos o SEU serviço de usuário aqui
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registrar")
    public ResponseEntity<Void> registrar(@RequestBody RegistrarTentativaDTO dto) {

        // Se o token for inválido, ele já lança o erro 401 sozinho.
        Usuario usuarioLogado = usuarioService.verificarAutenticacao();

        // Pega o ID do objeto retornado e passa para o histórico
        historicoService.registrarTentativa(
                usuarioLogado.getId(),
                dto.questaoId(),
                dto.acertou(),
                dto.topicos()
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/total")
    public ResponseEntity<Long> getTotalResolvidas() {
        Usuario usuarioLogado = usuarioService.verificarAutenticacao();
        long total = historicoService.contarQuestoesResolvidas(usuarioLogado.getId());
        return ResponseEntity.ok(total);
    }

    @GetMapping("/estatisticas/gerais")
    public ResponseEntity<EstatisticasGeraisDTO> getEstatisticasGerais() {
        Usuario usuarioLogado = usuarioService.verificarAutenticacao();
        return ResponseEntity.ok(historicoService.buscarEstatisticasGerais(usuarioLogado.getId()));
    }

    @GetMapping("/estatisticas/desempenho-materia")
    public ResponseEntity<List<DesempenhoMateriaDTO>> getDesempenhoMateria() {
        Usuario usuarioLogado = usuarioService.verificarAutenticacao();
        List<DesempenhoMateriaDTO> dados = historicoService.buscarDesempenhoPorMateria(usuarioLogado.getId());
        return ResponseEntity.ok(dados);
    }

    @GetMapping("/estatisticas/evolucao-semanal")
    public ResponseEntity<List<VolumeEstudo>> getEvolucaoSemanal() {
        Usuario usuarioLogado = usuarioService.verificarAutenticacao();
        return ResponseEntity.ok(historicoService.buscarEvolucaoSemanal(usuarioLogado.getId()));
    }

    @PostMapping("/registrar-lote")
    public ResponseEntity<Void> registrarLote(@RequestBody List<RegistrarTentativaDTO> dtos) {
        Usuario usuarioLogado = usuarioService.verificarAutenticacao();
        historicoService.registrarTentativasEmLote(usuarioLogado.getId(), dtos);
        return ResponseEntity.ok().build();
    }
}