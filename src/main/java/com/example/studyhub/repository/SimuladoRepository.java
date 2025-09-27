package com.example.studyhub.repository;

import com.example.studyhub.model.Simulado;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SimuladoRepository extends MongoRepository<Simulado, String> {
    List<Simulado> findByIdUser(String idUser);
    Optional<Simulado> findByIdAndIdUser(String id, String idUser);

    long countByIdUser(String idUser);
}
