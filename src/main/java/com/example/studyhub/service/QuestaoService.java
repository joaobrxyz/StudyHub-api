package com.example.studyhub.service;

import com.example.studyhub.model.Questao;
import com.example.studyhub.model.Dificuldade;
import com.example.studyhub.repository.QuestaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<Questao> buscarFiltrado(String disciplina, Dificuldade dificuldade, String instituicao, String ano, String termo, Integer page, Integer size ) {
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

        // Garante que o número da página e o tamanho são válidos
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 20; // Padrão 20

        // 1. Calcular quantos documentos pular (offset)
        long skipCount = (long) pageNumber * pageSize;

        // 2. Aplicar SKIP (Pular) e LIMIT (Tamanho da página)
        query.skip(skipCount);
        query.limit(pageSize);

        // Executa a query
        return mongoTemplate.find(query, Questao.class);
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
