package com.example.studyhub.service;

import com.example.studyhub.dto.GerarSimuladoDTO;
import com.example.studyhub.dto.SimuladoDTO;
import com.example.studyhub.model.*;
import com.example.studyhub.repository.QuestaoRepository;
import com.example.studyhub.repository.SimuladoRepository;
import com.example.studyhub.repository.TentativaSimuladoRepository;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SimuladoService {
    @Autowired
    private SimuladoRepository repository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private TentativaSimuladoRepository tentativaSimuladoRepository;

    @Autowired
    private HistoricoQuestoesService historicoService;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Simulado gerarSimuladoAutomatico(GerarSimuladoDTO dto, Usuario usuario) {
        // --- 1. VALIDAÇÃO DE LIMITE E AUTO-RESET MENSAL ---
        if (!usuario.isPremium()) {
            String mesAtual = java.time.YearMonth.now().toString(); // Retorna ex: "2026-01"

            // Se o mês mudou desde a última atividade, resetamos o saldo para 2
            if (!mesAtual.equals(usuario.getMesReferenciaLimite())) {
                usuario.setSimuladosAutomaticosRestantes(2);
                usuario.setMesReferenciaLimite(mesAtual);
                // Salva o reset imediatamente para evitar problemas de concorrência
                mongoTemplate.save(usuario);
            }

            // Verifica se ainda tem saldo para gerar automático
            if (usuario.getSimuladosAutomaticosRestantes() <= 0) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Limite de 2 simulados automáticos por mês atingido. Torne-se PRO para ter acesso ilimitado!");
            }
        }

        List<String> idsQuestoes = new ArrayList<>();

        // --- 2. MONTAGEM DOS FILTROS (CRITERIA) ---
        if (dto.quantidade() > 0) {
            List<Criteria> criteriosAnd = new ArrayList<>();

            // Filtro: Disciplina/Tópicos
            if (dto.disciplinas() != null && !dto.disciplinas().isEmpty()) {
                List<Criteria> orDisciplina = new ArrayList<>();
                for (String disc : dto.disciplinas()) {
                    Pattern pattern = Pattern.compile(".*" + Pattern.quote(disc) + ".*", Pattern.CASE_INSENSITIVE);
                    orDisciplina.add(Criteria.where("disciplina").regex(pattern));
                    orDisciplina.add(Criteria.where("topicos").regex(pattern));
                }
                criteriosAnd.add(new Criteria().orOperator(orDisciplina));
            }

            // Filtro: Dificuldade
            if (dto.dificuldades() != null && !dto.dificuldades().isEmpty()) {
                criteriosAnd.add(Criteria.where("dificuldade").in(dto.dificuldades()));
            }

            // Filtro: Instituição
            if (dto.instituicoes() != null && !dto.instituicoes().isEmpty()) {
                List<Criteria> orInstituicao = new ArrayList<>();
                for (String inst : dto.instituicoes()) {
                    Pattern pattern = Pattern.compile(".*" + Pattern.quote(inst) + ".*", Pattern.CASE_INSENSITIVE);
                    orInstituicao.add(Criteria.where("instituicao").regex(pattern));
                }
                criteriosAnd.add(new Criteria().orOperator(orInstituicao));
            }

            // Filtro: Ano
            if (dto.anos() != null && !dto.anos().isEmpty()) {
                criteriosAnd.add(Criteria.where("ano").in(dto.anos()));
            }

            // --- 3. FILTROS PREMIUM (ERROS E RESOLUÇÕES) ---
            // Só aplica se o usuário for premium. Se não for, o Java ignora silenciosamente.
            if (usuario.isPremium()) {
                if (Boolean.TRUE.equals(dto.apenasErros())) {
                    List<String> idsErrados = historicoService.buscarIdsQuestoesErradas(usuario.getId());
                    if (idsErrados.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Você ainda não possui questões erradas para filtrar.");
                    }
                    criteriosAnd.add(Criteria.where("_id").in(idsErrados));
                }

                if (Boolean.TRUE.equals(dto.comResolucao())) {
                    criteriosAnd.add(Criteria.where("resolucao").exists(true).ne(""));
                }

                if (Boolean.TRUE.equals(dto.comVideo())) {
                    criteriosAnd.add(Criteria.where("resolucaoVideoId").exists(true).ne(""));
                }
            }

            // --- 4. EXECUÇÃO DA AGREGAÇÃO (SORTEIO) ---
            Criteria matchCriteria = new Criteria();
            if (!criteriosAnd.isEmpty()) {
                matchCriteria.andOperator(criteriosAnd);
            }

            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(matchCriteria),
                    Aggregation.sample(dto.quantidade())
            );

            AggregationResults<Questao> results = mongoTemplate.aggregate(aggregation, "questoes", Questao.class);
            List<Questao> questoesSorteadas = results.getMappedResults();

            if (questoesSorteadas.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma questão encontrada com esses filtros.");
            }

            idsQuestoes = questoesSorteadas.stream().map(Questao::getId).collect(Collectors.toList());
        }

        // --- 5. SALVAMENTO DO SIMULADO ---
        Dificuldade dificuldade = calcularNivelAutomatico(idsQuestoes);

        Simulado simulado = new Simulado();
        simulado.setNome(dto.titulo());
        simulado.setDescricao(dto.descricao());
        simulado.setIdUser(usuario.getId());
        simulado.setData(LocalDateTime.now());
        simulado.setDificuldade(dificuldade);
        simulado.setQuestoes(idsQuestoes);

        Simulado salvo = repository.save(simulado);

        // --- 6. DEDUÇÃO DO SALDO (SE NÃO FOR PREMIUM) ---
        if (!usuario.isPremium()) {
            usuario.setSimuladosAutomaticosRestantes(usuario.getSimuladosAutomaticosRestantes() - 1);
            mongoTemplate.save(usuario);
        }

        return salvo;
    }

    public List<Simulado> listarPorUsuario(String idUser) {
        // Usa o método do Spring Data que filtra pelo campo 'idUser'
        return repository.findByIdUserOrderByDataDesc(idUser);
    }

    public Simulado criar(SimuladoDTO dto, String idUser, boolean isPublico, Dificuldade dificuldade) {
        Simulado simulado = new Simulado(dto.nome(), dto.descricao(), idUser, dto.questoes(), isPublico, dificuldade);
        return repository.save(simulado);
    }

    public Optional<Simulado> buscarPorIdEUsuario(String simuladoId, String idUser) {
        return repository.findByIdAndIdUser(simuladoId, idUser);
    }

    public void atualizar(String id, Simulado dadosNovos, Dificuldade dificuldade) {
        Simulado simuladoExistente = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Simulado não encontrado"));

        // Atualiza os campos permitidos
        simuladoExistente.setNome(dadosNovos.getNome());
        simuladoExistente.setDescricao(dadosNovos.getDescricao());
        simuladoExistente.setDificuldade(dificuldade);

        // Atualiza a lista de questões (caso tenha removido alguma)
        if (dadosNovos.getQuestoes() != null) {
            simuladoExistente.setQuestoes(dadosNovos.getQuestoes());
        }


        repository.save(simuladoExistente);
    }

    public List<Simulado> listarPublicos() {
        return repository.findByPublicoTrue();
    }

    public boolean deletarPorId(String simuladoId, String idUser) {

        // 1. Busca o simulado pelo ID
        Optional<Simulado> simuladoOpt = repository.findById(simuladoId);

        if (simuladoOpt.isEmpty()) {
            return false; // Não encontrado
        }

        Simulado simulado = simuladoOpt.get();

        // 2. Verifica se o usuário logado é o dono (dono do simulado == usuário logado)
        if (simulado.getIdUser().equals(idUser)) {
            repository.deleteById(simuladoId); // Exclui
            return true; // Sucesso
        } else {
            // O simulado existe, mas o usuário logado NÃO é o dono.
            // Retornamos false, e o Controller interpreta como 404 (para não expor a existência do simulado).
            return false;
        }
    }

    public long contarSimuladosPorUsuario(String idUser) {
        return repository.countByIdUser(idUser);
    }

    public Dificuldade calcularNivelAutomatico(List<String> idsQuestoes) {
        if (idsQuestoes == null || idsQuestoes.isEmpty()) return Dificuldade.MEDIO;

        // Busca as questões para ler o campo 'dificuldade'
        List<Questao> questoes = mongoTemplate.find(
                Query.query(Criteria.where("_id").in(idsQuestoes)), Questao.class
        );

        double somaPesos = 0;
        for (Questao q : questoes) {

            String diff = q.getDificuldade().toString();

            if ("FACIL".equals(diff)) somaPesos += 1;
            else if ("MEDIO".equals(diff)) somaPesos += 2;
            else if ("DIFICIL".equals(diff)) somaPesos += 3;
        }

        double media = somaPesos / questoes.size();

        // Lógica de classificação baseada na média dos pesos
        if (media <= 1.5) return Dificuldade.FACIL;
        if (media <= 2.5) return Dificuldade.MEDIO;
        return Dificuldade.DIFICIL;
    }

    public TentativaSimulado salvarTentativa(String usuarioId, String simuladoId, TentativaSimulado tentativa) {
        var simulado = repository.findById(simuladoId);
        tentativa.setUsuarioId(usuarioId);
        tentativa.setSimuladoId(simuladoId);
        tentativa.setNomeSimulado(simulado.get().getNome());
        // Regra de Negócio: Calcula a porcentagem de acertos
        if (tentativa.getTotalQuestoes() > 0) {
            double calculo = (double) tentativa.getTotalAcertos() / tentativa.getTotalQuestoes() * 100;
            tentativa.setPorcentagemAcerto(calculo);
        }

        tentativa.setDataFim(LocalDateTime.now()); // Garante a data atual
        return tentativaSimuladoRepository.save(tentativa);
    }

    public List<TentativaSimulado> listarHistoricoPorSimulado(String usuarioId, String simuladoId) {
        return tentativaSimuladoRepository.findByUsuarioIdAndSimuladoIdOrderByDataFimDesc(usuarioId, simuladoId);
    }

    public Simulado prepararSimuladoSomenteErros(String simuladoId, String tentativaId, String usuarioId) {
        // 1. Busca a tentativa e valida se pertence ao usuário logado
        TentativaSimulado tentativaAnterior = tentativaSimuladoRepository.findById(tentativaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa não encontrada"));

        if (!tentativaAnterior.getUsuarioId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar esta tentativa.");
        }

        // 2. Filtra os IDs das questões incorretas
        List<String> idsErrados = tentativaAnterior.getDetalhesRespostas().stream()
                .filter(resposta -> !resposta.isCorreta())
                .map(RespostaDetalhe::getQuestaoId)
                .collect(Collectors.toList());

        if (idsErrados.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parabéns! Você não errou nenhuma questão nesta tentativa.");
        }

        // 3. Busca o original para herdar metadados
        Simulado original = repository.findById(simuladoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Simulado original não encontrado"));

        // 4. Monta o objeto temporário (Snapshot)
        Simulado simuladoReforco = new Simulado();
        simuladoReforco.setId(original.getId());
        simuladoReforco.setNome("Reforço: " + original.getNome());
        simuladoReforco.setDescricao("Foco exclusivo nas questões incorretas da tentativa anterior.");
        simuladoReforco.setQuestoes(idsErrados);
        simuladoReforco.setDificuldade(original.getDificuldade());
        simuladoReforco.setIdUser(usuarioId);

        return simuladoReforco;
    }

    public TentativaSimulado buscarTentativaPorId(String tentativaId, String usuarioId) {
        return tentativaSimuladoRepository.findById(tentativaId)
                .filter(t -> t.getUsuarioId().equals(usuarioId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa não encontrada ou acesso negado."));
    }

    public boolean adicionarQuestao(String simuladoId, String questaoId) {
        // Definimos o critério de busca (ID do simulado)
        Query query = new Query(Criteria.where("id").is(simuladoId));

        Update update = new Update().addToSet("questoes", questaoId);

        // Executa a atualização e pega o resultado
        UpdateResult result = mongoTemplate.updateFirst(query, update, Simulado.class);

        // Se o 'ModifiedCount' for maior que 0, significa que a questão foi adicionada.
        // Se for 0, ou o simulado não existe, ou a questão já estava lá.
        if (result.getModifiedCount() > 0) {
            return true;
        } else {
            return false;
        }
    }
}
