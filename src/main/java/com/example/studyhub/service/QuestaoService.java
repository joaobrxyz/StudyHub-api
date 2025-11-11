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

@Service
public class QuestaoService {
    @Autowired
    private QuestaoRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Questao> criarQuestoes(List<Questao> questoes) {
        return repository.saveAll(questoes);
    }

    public Page<Questao> buscarFiltrado(String disciplina, Dificuldade dificuldade, String instituicao, String ano, String termo, Pageable paginacao) {
        // Cria um objeto Query
        Query query = new Query();

        // Esta expressão regular garante a busca PARCIAL e CASE-INSENSITIVE
        // Ex: Se o termo for "java", ele busca ".*java.*"

        // 1. FILTRO DE DISCIPLINA (Busca parcial)
        if (StringUtils.hasText(disciplina)) {
            Pattern pattern = Pattern.compile(
                    ".*" + Pattern.quote(disciplina) + ".*",
                    Pattern.CASE_INSENSITIVE
            );
            query.addCriteria(Criteria.where("disciplina").regex(pattern));
        }

        // 2. FILTRO DE DIFICULDADE (Busca exata, pois é um ENUM)
        if (dificuldade != null) {
            query.addCriteria(Criteria.where("dificuldade").is(dificuldade.toString()));
        }

        // 3. FILTRO DE INSTITUIÇÃO (Busca parcial)
        if (StringUtils.hasText(instituicao)) {
            Pattern pattern = Pattern.compile(
                    ".*" + Pattern.quote(instituicao) + ".*",
                    Pattern.CASE_INSENSITIVE
            );
            query.addCriteria(Criteria.where("instituicao").regex(pattern));
        }

        // 4. FILTRO DE ANO (Busca exata, pois é um valor específico)
        if (StringUtils.hasText(ano)) {
            query.addCriteria(Criteria.where("ano").is(ano));
        }

        // 5. FILTRO DE TERMO (Busca parcial no enunciado)
        if (StringUtils.hasText(termo)) {
            Pattern pattern = Pattern.compile(
                    ".*" + Pattern.quote(termo) + ".*",
                    Pattern.CASE_INSENSITIVE
            );
            query.addCriteria(Criteria.where("enunciado").regex(pattern));
        }

        // A) Contar o total de documentos que atendem aos critérios (essencial para metadados da Page)
        long total = mongoTemplate.count(query, Questao.class);

        // B) Aplicar os parâmetros de ordenação (sort), skip e limit do Pageable na Query
        query.with(paginacao);

        // C) Executar a busca paginada
        List<Questao> questoes = mongoTemplate.find(query, Questao.class);

        // D) Retornar o objeto Page<Questao> completo
        // PageImpl é a implementação concreta da interface Page.
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
}
