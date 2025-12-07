package com.example.studyhub.service;

import com.example.studyhub.model.Questao;
import com.example.studyhub.model.Dificuldade;
import com.example.studyhub.repository.QuestaoRepository;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Map;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class QuestaoService {
    @Autowired
    private QuestaoRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public List<Questao> criarQuestoes(List<Questao> questoes) {
        return repository.saveAll(questoes);
    }

    public Page<Questao> buscarFiltrado(
            List<String> disciplinas,
            List<Dificuldade> dificuldades,
            List<String> instituicoes,
            List<String> anos,
            String termo,
            Pageable paginacao
    ) {
        Query query = new Query();

        // 1. FILTRO DE DISCIPLINA (Agora procura em 'disciplina' OU 'topicos')
        if (disciplinas != null && !disciplinas.isEmpty()) {
            List<Criteria> orCriteria = new ArrayList<>();

            for (String disc : disciplinas) {
                // Cria o padrão regex (contém, case-insensitive)
                Pattern pattern = Pattern.compile(".*" + Pattern.quote(disc) + ".*", Pattern.CASE_INSENSITIVE);

                // Adiciona critério para o campo disciplina
                orCriteria.add(Criteria.where("disciplina").regex(pattern));
                // Adiciona critério para o campo topicos (MongoDB lida com array automaticamente)
                orCriteria.add(Criteria.where("topicos").regex(pattern));
            }

            // Adiciona um grande OR: (disciplina ~ Mat OR topicos ~ Mat OR disciplina ~ Fis ...)
            query.addCriteria(new Criteria().orOperator(orCriteria));
        }

        // 2. FILTRO DE DIFICULDADE (Mantém busca exata)
        if (dificuldades != null && !dificuldades.isEmpty()) {
            List<String> dificuldadesStr = dificuldades.stream().map(Enum::toString).collect(Collectors.toList());
            query.addCriteria(Criteria.where("dificuldade").in(dificuldadesStr));
        }

        // 3. FILTRO DE INSTITUIÇÃO (Mantém sua lógica auxiliar original)
        if (instituicoes != null && !instituicoes.isEmpty()) {
            addMultiValueStringCriteria(query, "instituicao", instituicoes);
        }

        // 4. FILTRO DE ANO (Mantém busca exata)
        if (anos != null && !anos.isEmpty()) {
            query.addCriteria(Criteria.where("ano").in(anos));
        }

        // 5. FILTRO DE TERMO (Busca geral: Enunciado, Disciplina, Instituição E TÓPICOS)
        if (StringUtils.hasText(termo)) {
            Pattern pattern = Pattern.compile(
                    ".*" + Pattern.quote(termo) + ".*",
                    Pattern.CASE_INSENSITIVE
            );

            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("enunciado").regex(pattern),
                    Criteria.where("disciplina").regex(pattern),
                    Criteria.where("instituicao").regex(pattern),
                    Criteria.where("topicos").regex(pattern)
            ));
        }

        // A) Contar
        long total = mongoTemplate.count(query, Questao.class);

        // B) Paginação
        query.with(paginacao);

        // C) Buscar
        List<Questao> questoes = mongoTemplate.find(query, Questao.class);

        return new PageImpl<>(questoes, paginacao, total);
    }

    public Questao buscarPorId(String id) {
        return repository.findById(id).orElse(null);
    }

    public ResponseEntity<String> deletarPorId(String id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok("Questão deletada com sucesso!");
        } else {
            return ResponseEntity.status(404).body("Questão não encontrada!");
        }
    }

    private void addMultiValueStringCriteria(
            Query query,
            String fieldName,
            List<String> values
    ) {
        if (values != null && !values.isEmpty()) {
            // Se houver apenas um valor, aplicamos a busca parcial e case-insensitive (regex)
            if (values.size() == 1) {
                String value = values.get(0);
                Pattern pattern = Pattern.compile(".*" + Pattern.quote(value) + ".*", Pattern.CASE_INSENSITIVE);
                query.addCriteria(Criteria.where(fieldName).regex(pattern));
                return;
            }

            // Se houver MÚLTIPLOS VALORES, criamos uma cláusula OR
            // Ex: (disciplina ~ 'Fisica') OR (disciplina ~ 'Quimica')
            List<Criteria> orCriteriaList = values.stream()
                    .map(value -> {
                        Pattern pattern = Pattern.compile(".*" + Pattern.quote(value) + ".*", Pattern.CASE_INSENSITIVE);
                        return Criteria.where(fieldName).regex(pattern);
                    })
                    .collect(java.util.stream.Collectors.toList());

            // Adiciona o grande OR ao critério principal
            query.addCriteria(new Criteria().orOperator(orCriteriaList));
        }
    }

    public Questao atualizarParcial(String id, Map<String, Object> updates) {
        // 1. Buscar a questão existente
        Questao questaoExistente = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Questão não encontrada."));

        // 2. Validar e converter a DIFICULDADE (se presente)
        if (updates.containsKey("dificuldade")) {
            Object dificuldadeValue = updates.get("dificuldade");
            if (dificuldadeValue instanceof String) {
                try {
                    Dificuldade dificuldadeEnum = Dificuldade.valueOf(((String) dificuldadeValue).toUpperCase());
                    updates.put("dificuldade", dificuldadeEnum);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Valor de dificuldade inválido: " + dificuldadeValue);
                }
            }
        }

        // 3. Aplicar os campos atualizados usando ObjectMapper com tratamento de exceção
        try {
            // A linha onde a exceção ocorre
            Questao questaoAtualizada = objectMapper.updateValue(questaoExistente, updates);

            // 4. Salvar e retornar
            return repository.save(questaoAtualizada);

        } catch (JsonMappingException e) {
            // Captura erros de mapeamento (Ex: tipo de dado errado, campo desconhecido)
            String errorMessage = "Erro ao mapear a atualização. Verifique se os campos e tipos de dados estão corretos. Detalhe: " + e.getOriginalMessage();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }
    public void atualizarEmLote(List<Map<String, Object>> listaUpdates) {
        for (Map<String, Object> updates : listaUpdates) {
            // 1. Extrai o ID
            String id = (String) updates.get("id");

            if (id != null) {
                // 2. Remove o ID do payload de atualização
                // Isso impede que o Jackson tente alterar a chave primária (o que é perigoso)
                // Como 'updates' é um Map mutável vindo do Controller, podemos remover direto.
                updates.remove("id");

                // 3. Chama a atualização parcial
                this.atualizarParcial(id, updates);
            }
        }
    }

    public List<Questao> buscarPorListaIds(List<String> ids) {
        return repository.findAllById(ids);
    }
}
