package com.example.studyhub.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.studyhub.dto.GerarSimuladoDTO;
import com.example.studyhub.dto.SimuladoDTO;
import com.example.studyhub.dto.SimuladoPublicoRetornoDTO;
import com.example.studyhub.dto.SimuladoRetornoDTO;
import com.example.studyhub.model.Dificuldade;
import com.example.studyhub.model.TentativaSimulado;
import com.example.studyhub.model.Usuario;
import com.example.studyhub.service.SimuladoService;
import com.example.studyhub.service.UsuarioService;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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

    @Autowired
    private MongoTemplate mongoTemplate;

    // Listar todos os simulados do usuário
    @GetMapping
    public ResponseEntity<List<SimuladoRetornoDTO>> listarSimuladosDoUsuario() {

        Usuario usuario = usuarioService.verificarAutenticacao();

        String usuarioId = usuario.getId();

        // Chama o serviço para filtrar
        List<Simulado> simulados = simuladoService.listarPorUsuario(usuarioId);

        List<SimuladoRetornoDTO> dto = simulados.stream()
                .map(SimuladoRetornoDTO::new)
                .toList();

        return ResponseEntity.ok(dto);
    }

    // Criar um novo simulado
    @PostMapping
    public ResponseEntity<Map<String, String>> criar(@RequestBody SimuladoDTO dto) {

        Usuario usuario = usuarioService.verificarAutenticacao();

        Dificuldade dificuldade = simuladoService.calcularNivelAutomatico(dto.questoes());

        simuladoService.criar(dto, usuario.getId(), false, dificuldade);

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

    @PostMapping("/publicos")
    public ResponseEntity<Map<String, String>> criarPublico(@RequestBody SimuladoDTO dto) {
        // Se não for ADMIN, o próprio service já barra com 401
        Usuario admin = usuarioService.verificarPermissaoAdmin();

        Dificuldade dificuldade = simuladoService.calcularNivelAutomatico(dto.questoes());
        simuladoService.criar(dto, admin.getId(), true, dificuldade);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap("message", "Simulado público criado com sucesso."));
    }
    // Buscar simulado por ID
    @GetMapping("/{id}")
    public ResponseEntity<SimuladoDTO> buscarPorId(@PathVariable String id) {

        Usuario usuario = usuarioService.verificarAutenticacao();

        String usuarioId = usuario.getId();

        // Chama o serviço para buscar e verificar a propriedade
        Optional<Simulado> simuladoOpt = simuladoService.buscarPorIdEUsuario(id, usuarioId);

        // Retorna o resultado
        if (simuladoOpt.isPresent()) {
            // 200 OK
            Simulado simulado = simuladoOpt.get();
            SimuladoDTO dto = new SimuladoDTO(simulado);
            return ResponseEntity.ok(dto);
        } else {
            // Se o simulado não existe OU se o simulado existe mas não pertence ao usuário: 404
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/publicos")
    public ResponseEntity<List<SimuladoPublicoRetornoDTO>> listarPublicos() {
        usuarioService.verificarAutenticacao();
        List<Simulado> simulados = simuladoService.listarPublicos();
        List<SimuladoPublicoRetornoDTO> dto = simulados.stream()
                .map(SimuladoPublicoRetornoDTO::new)
                .toList();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarSimulado(@PathVariable String id, @RequestBody Simulado simuladoAtualizado) {
        usuarioService.verificarAutenticacao();

        Dificuldade dificuldade = simuladoService.calcularNivelAutomatico(simuladoAtualizado.getQuestoes());

        simuladoService.atualizar(id, simuladoAtualizado, dificuldade);

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

    @PostMapping("/desempenho/{simuladoId}")
    public ResponseEntity<TentativaSimulado> finalizarSimulado(
            @PathVariable String simuladoId, // Pega o ID da URL
            @RequestBody TentativaSimulado tentativa) {

        // Pega o usuário autenticado (Segurança)
        Usuario usuario = usuarioService.verificarAutenticacao();

        return ResponseEntity.ok(simuladoService.salvarTentativa(usuario.getId(), simuladoId, tentativa));
    }

    @GetMapping("/desempenho/{simuladoId}")
    public ResponseEntity<List<TentativaSimulado>> obterHistorico(
            @PathVariable String simuladoId) {
        Usuario usuario = usuarioService.verificarAcessoPremium();
        String usuarioId = usuario.getId();
        return ResponseEntity.ok(simuladoService.listarHistoricoPorSimulado(usuarioId, simuladoId));
    }

    @GetMapping("/{simuladoId}/refazer-erros/{tentativaId}")
    public ResponseEntity<SimuladoDTO> gerarSimuladoDeErros(
            @PathVariable String simuladoId,
            @PathVariable String tentativaId) {

        Usuario usuario = usuarioService.verificarAcessoPremium();

        Simulado simuladoDeErros = simuladoService.prepararSimuladoSomenteErros(simuladoId, tentativaId, usuario.getId());

        SimuladoDTO dto = new SimuladoDTO(simuladoDeErros);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/tentativa/{tentativaId}")
    public ResponseEntity<TentativaSimulado> buscarTentativaPorId(@PathVariable String tentativaId) {
        Usuario usuario = usuarioService.verificarAcessoPremium();

        TentativaSimulado tentativa = simuladoService.buscarTentativaPorId(tentativaId, usuario.getId());

        return ResponseEntity.ok(tentativa);
    }

    @PatchMapping("/{id}/adicionar-questao/{questaoId}")
    public ResponseEntity<Map<String, String>> adicionarQuestao(
            @PathVariable String id,
            @PathVariable String questaoId) {

        // Garante que apenas usuários logados acessem
        usuarioService.verificarAutenticacao();

        // Chama a nova lógica atômica do Service
        boolean adicionada = simuladoService.adicionarQuestao(id, questaoId);

        if (adicionada) {
            // Retorno 200 OK se a questão foi inserida com sucesso
            return ResponseEntity.ok(Collections.singletonMap("message", "Questão adicionada!"));
        } else {
            // Retorno 409 Conflict se a questão já existia ou o ID é inválido
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", "Esta questão já faz parte do simulado."));
        }
    }
}
