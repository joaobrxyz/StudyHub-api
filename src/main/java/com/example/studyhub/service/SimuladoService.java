package com.example.studyhub.service;

import com.example.studyhub.dto.GerarSimuladoDTO;
import com.example.studyhub.model.Questao;
import com.example.studyhub.model.Simulado;
import com.example.studyhub.model.Usuario;
import com.example.studyhub.repository.SimuladoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
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
    private MongoTemplate mongoTemplate;

    public Simulado gerarSimuladoAutomatico(GerarSimuladoDTO dto, Usuario usuario) {
        List<String> idsQuestoes = new ArrayList<>();

        // SÓ ENTRA NA LÓGICA DE SORTEIO SE A QUANTIDADE FOR MAIOR QUE 0
        if (dto.quantidade() > 0) {
            // Lista para acumular todos os filtros (AND)
            List<Criteria> criteriosAnd = new ArrayList<>();

            // 1. FILTRO DE DISCIPLINA
            // Procura em 'disciplina' OU 'topicos' usando Regex
            if (dto.disciplinas() != null && !dto.disciplinas().isEmpty()) {
                List<Criteria> orDisciplina = new ArrayList<>();

                for (String disc : dto.disciplinas()) {
                    Pattern pattern = Pattern.compile(".*" + Pattern.quote(disc) + ".*", Pattern.CASE_INSENSITIVE);

                    orDisciplina.add(Criteria.where("disciplina").regex(pattern));
                    orDisciplina.add(Criteria.where("topicos").regex(pattern));
                }

                // Adiciona ao grupo principal como um bloco OR
                criteriosAnd.add(new Criteria().orOperator(orDisciplina));
            }

            // 2. FILTRO DE DIFICULDADE
            if (dto.dificuldades() != null && !dto.dificuldades().isEmpty()) {
                criteriosAnd.add(Criteria.where("dificuldade").in(dto.dificuldades()));
            }

            // 3. FILTRO DE INSTITUIÇÃO
            if (dto.instituicoes() != null && !dto.instituicoes().isEmpty()) {
                List<Criteria> orInstituicao = new ArrayList<>();

                for (String inst : dto.instituicoes()) {
                    Pattern pattern = Pattern.compile(".*" + Pattern.quote(inst) + ".*", Pattern.CASE_INSENSITIVE);
                    orInstituicao.add(Criteria.where("instituicao").regex(pattern));
                }

                criteriosAnd.add(new Criteria().orOperator(orInstituicao));
            }

            // 4. FILTRO DE ANO (Busca exata)
            if (dto.anos() != null && !dto.anos().isEmpty()) {
                criteriosAnd.add(Criteria.where("ano").in(dto.anos()));
            }

            // Cria o critério final combinando tudo com AND
            Criteria matchCriteria = new Criteria();
            if (!criteriosAnd.isEmpty()) {
                matchCriteria.andOperator(criteriosAnd);
            }

            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(matchCriteria), // Aplica os filtros inteligentes
                    Aggregation.sample(dto.quantidade()) // Sorteia X questões
            );

            // Executa no Mongo
            AggregationResults<Questao> results = mongoTemplate.aggregate(
                    aggregation, "questoes", Questao.class
            );

            List<Questao> questoesSorteadas = results.getMappedResults();

            // Validação
            if (questoesSorteadas.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma questão encontrada com esses filtros.");
            }

            idsQuestoes = questoesSorteadas.stream()
                    .map(Questao::getId)
                    .collect(Collectors.toList());
        }

        // Salva o Simulado
        Simulado simulado = new Simulado();
        simulado.setNome(dto.titulo());
        simulado.setDescricao(dto.descricao());
        simulado.setIdUser(usuario.getId());
        simulado.setData(LocalDateTime.now());

        simulado.setQuestoes(idsQuestoes);

        return repository.save(simulado);
    }

    public List<Simulado> listarPorUsuario(String idUser) {
        // Usa o método do Spring Data que filtra pelo campo 'idUser'
        return repository.findByIdUser(idUser);
    }

    public Simulado criar(Simulado simulado) {
        return repository.save(simulado);
    }

    public Optional<Simulado> buscarPorIdEUsuario(String simuladoId, String idUser) {
        return repository.findByIdAndIdUser(simuladoId, idUser);
    }

    public void atualizar(String id, Simulado dadosNovos) {
        Simulado simuladoExistente = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Simulado não encontrado"));

        // Atualiza os campos permitidos
        simuladoExistente.setNome(dadosNovos.getNome());
        simuladoExistente.setDescricao(dadosNovos.getDescricao());

        // Atualiza a lista de questões (caso tenha removido alguma)
        if (dadosNovos.getQuestoes() != null) {
            simuladoExistente.setQuestoes(dadosNovos.getQuestoes());
        }

        repository.save(simuladoExistente);
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
}
