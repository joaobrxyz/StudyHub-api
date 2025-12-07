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
import org.springframework.data.mongodb.core.query.Collation;
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

    public Page<Questao> buscarFiltrado(List<String> disciplinas,
                                        List<Dificuldade> dificuldades,
                                        List<String> instituicoes,
                                        List<String> anos,
                                        String termo,
                                        Pageable pageable) {

        Query query = new Query().with(pageable);

        // --- COLATION: O Segredo dos Acentos ---
        // Define a "força" da comparação como PRIMARY.
        // Isso faz o Mongo ignorar acentos e caixa alta (a == A == á == Á)
        // OBS: Certifique-se que sua versão do Mongo suporta Collation (3.4+)
        query.collation(Collation.of("pt").strength(1));

        List<Criteria> criteriosAnd = new ArrayList<>();

        // 1. TERMO DE BUSCA (Busca Geral - Enunciado, Disciplina, Tópicos, Instituição)
        if (termo != null && !termo.trim().isEmpty()) {
            String termoLimpo = Pattern.quote(termo.trim());
            Criteria criteriaTermo = new Criteria().orOperator(
                    Criteria.where("enunciado").regex(termoLimpo, "i"),
                    Criteria.where("disciplina").regex(termoLimpo, "i"),
                    Criteria.where("topicos").regex(termoLimpo, "i"),
                    Criteria.where("instituicao").regex(termoLimpo, "i")
            );
            criteriosAnd.add(criteriaTermo);
        }

        // 2. FILTRO DE DISCIPLINA (Lista: item1 OU item2 OU item3...)
        // Verifica: (Disciplina == item) OU (Tópicos contêm item)
        if (disciplinas != null && !disciplinas.isEmpty()) {
            List<Criteria> orDisciplinas = new ArrayList<>();

            for (String disc : disciplinas) {
                // Regex flexível para achar trechos (ex: "Matemática" acha "Matemática Aplicada")
                String regex = Pattern.quote(disc.trim());

                orDisciplinas.add(new Criteria().orOperator(
                        Criteria.where("disciplina").regex(regex, "i"),
                        Criteria.where("topicos").regex(regex, "i")
                ));
            }
            // Adiciona o bloco (A ou B ou C) na lista principal
            criteriosAnd.add(new Criteria().orOperator(orDisciplinas));
        }

        // 3. INSTITUIÇÃO (Lista: item1 OU item2...)
        if (instituicoes != null && !instituicoes.isEmpty()) {
            List<Criteria> orInstituicoes = new ArrayList<>();

            for (String inst : instituicoes) {
                // Regex Exato (^...$): Garante que "ENEM" não traga "ENEM PPL" se não quiser
                // O "i" junto com o Collation garante que maiúsculas/minúsculas não importem
                String regex = "^" + Pattern.quote(inst.trim()) + "$";
                orInstituicoes.add(Criteria.where("instituicao").regex(regex, "i"));
            }

            criteriosAnd.add(new Criteria().orOperator(orInstituicoes));
        }

        // 4. DIFICULDADE (Enum - Valor Exato)
        if (dificuldades != null && !dificuldades.isEmpty()) {
            criteriosAnd.add(Criteria.where("dificuldade").in(dificuldades));
        }

        // 5. ANO (Valor Exato)
        if (anos != null && !anos.isEmpty()) {
            criteriosAnd.add(Criteria.where("ano").in(anos));
        }

        // --- FINALIZAÇÃO: Junta todos os blocos com AND ---
        // Exemplo: (Termo) AND (Disciplina A ou B) AND (Instituição X)
        if (!criteriosAnd.isEmpty()) {
            Criteria criteriaFinal = new Criteria().andOperator(
                    criteriosAnd.toArray(new Criteria[0])
            );
            query.addCriteria(criteriaFinal);
        }

        long count = mongoTemplate.count(Query.of(query).limit(0).skip(0), Questao.class);
        List<Questao> questoes = mongoTemplate.find(query, Questao.class);

        return new PageImpl<>(questoes, pageable, count);
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
