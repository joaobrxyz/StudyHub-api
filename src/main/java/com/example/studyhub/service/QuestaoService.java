package com.example.studyhub.service;

import com.example.studyhub.model.Questao;
import com.example.studyhub.model.Dificuldade;
import com.example.studyhub.repository.QuestaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class QuestaoService {
    @Autowired
    private QuestaoRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

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

        if (disciplinas != null && !disciplinas.isEmpty()) {
            addMultiValueStringCriteria(query, "disciplina", disciplinas);
        }

        // 2. FILTRO DE DIFICULDADE (Busca Exata via $in)
        if (dificuldades != null && !dificuldades.isEmpty()) {
            List<String> dificuldadesStr = dificuldades.stream().map(Enum::toString).collect(Collectors.toList());
            query.addCriteria(Criteria.where("dificuldade").in(dificuldadesStr));
        }

        // 3. FILTRO DE INSTITUIÇÃO (Busca Exata via $in)
        if (instituicoes != null && !instituicoes.isEmpty()) {
            addMultiValueStringCriteria(query, "instituicao", instituicoes);
        }

        // 4. FILTRO DE ANO (Busca Exata via $in)
        if (anos != null && !anos.isEmpty()) {
            query.addCriteria(Criteria.where("ano").in(anos));
        }

        // 5. FILTRO DE TERMO (Busca Parcial e Flexível, ÚNICA que usa regex)
        if (StringUtils.hasText(termo)) {
            Pattern pattern = Pattern.compile(
                    ".*" + Pattern.quote(termo) + ".*",
                    Pattern.CASE_INSENSITIVE
            );

            // Combinação de critérios OR (Enunciado, Disciplina, Instituição)
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("enunciado").regex(pattern),
                    Criteria.where("disciplina").regex(pattern),
                    Criteria.where("instituicao").regex(pattern)
            ));
        }
        // A) Contar o total de documentos que atendem aos critérios
        long total = mongoTemplate.count(query, Questao.class);

        // B) Aplicar os parâmetros de ordenação (sort), skip e limit do Pageable
        query.with(paginacao);

        // C) Executar a busca paginada
        List<Questao> questoes = mongoTemplate.find(query, Questao.class);

        // D) Retornar o objeto Page<Questao> completo
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
}
