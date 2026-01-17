package com.example.studyhub.repository;

import com.example.studyhub.model.HistoricoQuestoes;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HistoricoRepository extends MongoRepository<HistoricoQuestoes, String> {
    // Busca registros onde o status atual é "ERROU"
    List<HistoricoQuestoes> findByUsuarioIdAndStatusAtual(String usuarioId, String statusAtual);
}
