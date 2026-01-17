package com.example.studyhub.repository;

import com.example.studyhub.model.TentativaSimulado;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TentativaSimuladoRepository extends MongoRepository<TentativaSimulado, String> {
    // Busca o histórico de um simulado específico para o gráfico de evolução
    List<TentativaSimulado> findByUsuarioIdAndSimuladoIdOrderByDataFimDesc(String usuarioId, String simuladoId);

    // Busca todas as tentativas de um usuário
    List<TentativaSimulado> findByUsuarioIdOrderByDataFimDesc(String usuarioId);
}
