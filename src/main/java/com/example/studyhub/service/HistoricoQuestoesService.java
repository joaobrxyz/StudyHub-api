package com.example.studyhub.service;

import com.example.studyhub.dto.DesempenhoMateriaDTO;
import com.example.studyhub.dto.EstatisticasGeraisDTO;
import com.example.studyhub.dto.RegistrarTentativaDTO;
import com.example.studyhub.model.HistoricoQuestoes;
import com.example.studyhub.model.Usuario;
import com.example.studyhub.model.VolumeEstudo;
import com.example.studyhub.repository.HistoricoRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistoricoQuestoesService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HistoricoRepository historicoRepository;

    public void registrarTentativa(String usuarioId, String questaoId, boolean acertou, List<String> topicos) {

        // 1. Critério: Procura pelo User + Questão
        Query query = new Query(Criteria.where("usuarioId").is(usuarioId).and("questaoId").is(questaoId));

        // 2. Configura a atualização
        Update update = new Update();

        // Atualiza status e data (sobrescreve)
        update.set("statusAtual", acertou ? "ACERTOU" : "ERROU");
        update.set("ultimaResolucao", LocalDateTime.now());
        update.set("topicos", topicos);

        // INCREMENTA os contadores (+1)
        update.inc("tentativas", 1);

        if (acertou) {
            update.inc("acertos", 1);
        } else {
            update.inc("erros", 1);
        }

        // Usa a classe HistoricoQuestoes para mapear a coleção correta
        mongoTemplate.upsert(query, update, HistoricoQuestoes.class);

        Usuario usuario = mongoTemplate.findById(usuarioId, Usuario.class);
        if (usuario != null) {
            LocalDate hoje = LocalDate.now();
            LocalDate ultima = usuario.getDataUltimaAtividade();

            // Se ele ainda não estudou hoje
            if (ultima == null || !ultima.equals(hoje)) {
                if (ultima != null && ultima.equals(hoje.minusDays(1))) {
                    // Estudou ontem? Aumenta o streak
                    usuario.setStreakAtual(usuario.getStreakAtual() + 1);
                } else {
                    // Ficou um dia sem estudar? Reseta para 1
                    usuario.setStreakAtual(1);
                }
                usuario.setDataUltimaAtividade(hoje);
                mongoTemplate.save(usuario);
            }
        }
        Query queryVol = new Query(Criteria.where("usuarioId").is(usuarioId).and("data").is(LocalDate.now()));
        Update updateVol = new Update().inc("quantidade", 1); // +1 questão no dia de hoje

        mongoTemplate.upsert(queryVol, updateVol, VolumeEstudo.class);
    }

    public long contarQuestoesResolvidas(String usuarioId) {
        Query query = new Query(Criteria.where("usuarioId").is(usuarioId));
        return mongoTemplate.count(query, HistoricoQuestoes.class);
    }

    public EstatisticasGeraisDTO buscarEstatisticasGerais(String usuarioId) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("usuarioId").is(usuarioId)),
                Aggregation.group().sum("tentativas").as("totalTentativas").sum("acertos").as("totalAcertos")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "historico_questoes", org.bson.Document.class);
        org.bson.Document resultDoc = results.getUniqueMappedResult();

        if (resultDoc == null) {
            return new EstatisticasGeraisDTO(0, 0);
        }

        return new EstatisticasGeraisDTO(
                resultDoc.getInteger("totalTentativas").longValue(),
                resultDoc.getInteger("totalAcertos").longValue()
        );
    }
    public List<DesempenhoMateriaDTO> buscarDesempenhoPorMateria(String usuarioId) {
        Aggregation agg = Aggregation.newAggregation(
                // 1. Filtra pelo usuário
                Aggregation.match(Criteria.where("usuarioId").is(usuarioId)),

                // 2. Extrai a matéria
                Aggregation.project("acertos", "erros")
                        .and(ArrayOperators.ArrayElemAt.arrayOf("topicos").elementAt(1)).as("materia"),

                // 3. Agrupa e CRIA um campo de contagem total
                Aggregation.group("materia")
                        .sum("acertos").as("totalAcertos")
                        .sum("erros").as("totalErros")
                        .count().as("totalAtividades"), // Soma quantos registros existem por matéria

                // 4. ORDENA pelo maior volume de questões primeiro
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "totalAtividades"),

                // 5. LIMITA às 10 matérias com mais interações
                Aggregation.limit(5)
        );

        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(agg, "historico_questoes", org.bson.Document.class);

        return results.getMappedResults().stream().map(doc -> {
            Object idBruto = doc.get("_id");
            String materiaNome = (idBruto != null) ? idBruto.toString() : "Geral";

            double a = ((Number) doc.getOrDefault("totalAcertos", 0)).doubleValue();
            double e = ((Number) doc.getOrDefault("totalErros", 0)).doubleValue();
            double total = a + e;

            return new DesempenhoMateriaDTO(
                    materiaNome,
                    total > 0 ? (a / total) * 100 : 0,
                    total > 0 ? (e / total) * 100 : 0
            );
        }).toList();
    }
    public List<VolumeEstudo> buscarEvolucaoSemanal(String usuarioId) {
        LocalDate seteDiasAtras = LocalDate.now().minusDays(6);
        Query query = new Query(Criteria.where("usuarioId").is(usuarioId)
                .and("data").gte(seteDiasAtras))
                .with(Sort.by(Sort.Direction.ASC, "data"));
        return mongoTemplate.find(query, VolumeEstudo.class);
    }
    public List<String> buscarIdsQuestoesErradas(String usuarioId) {
        // Busca todos os registros onde o status mais recente gravado é "ERROU"
        List<HistoricoQuestoes> registrosDeErro = historicoRepository.findByUsuarioIdAndStatusAtual(usuarioId, "ERROU");

        // Extrai os IDs das questões
        return registrosDeErro.stream()
                .map(HistoricoQuestoes::getQuestaoId)
                .distinct()
                .collect(Collectors.toList());
    }

    public void registrarTentativasEmLote(String usuarioId, List<RegistrarTentativaDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        // Itera sobre o lote para aplicar os incrementos individuais de cada questão
        for (RegistrarTentativaDTO dto : dtos) {
            Query query = new Query(Criteria.where("usuarioId").is(usuarioId).and("questaoId").is(dto.questaoId()));
            Update update = new Update();

            update.set("statusAtual", dto.acertou() ? "ACERTOU" : "ERROU");
            update.set("ultimaResolucao", LocalDateTime.now());
            update.set("topicos", dto.topicos());
            update.inc("tentativas", 1);

            if (dto.acertou()) {
                update.inc("acertos", 1);
            } else {
                update.inc("erros", 1);
            }

            mongoTemplate.upsert(query, update, HistoricoQuestoes.class);
        }

        // Lógica de Streak: Atualiza apenas uma vez por lote
        Usuario usuario = mongoTemplate.findById(usuarioId, Usuario.class);
        if (usuario != null) {
            LocalDate hoje = LocalDate.now();
            LocalDate ultima = usuario.getDataUltimaAtividade();

            if (ultima == null || !ultima.equals(hoje)) {
                if (ultima != null && ultima.equals(hoje.minusDays(1))) {
                    usuario.setStreakAtual(usuario.getStreakAtual() + 1);
                } else {
                    usuario.setStreakAtual(1);
                }
                usuario.setDataUltimaAtividade(hoje);
                mongoTemplate.save(usuario);
            }
        }

        // Lógica de Volume: Incrementa o total de questões do lote de uma vez
        Query queryVol = new Query(Criteria.where("usuarioId").is(usuarioId).and("data").is(LocalDate.now()));
        Update updateVol = new Update().inc("quantidade", dtos.size());
        mongoTemplate.upsert(queryVol, updateVol, VolumeEstudo.class);
    }
}
