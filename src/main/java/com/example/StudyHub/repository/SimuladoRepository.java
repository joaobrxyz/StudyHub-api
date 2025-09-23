package com.example.studyhub.repository;

import com.example.studyhub.model.Simulado;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SimuladoRepository extends MongoRepository<Simulado, String> {
}
