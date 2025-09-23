package com.example.studyhub.repository;

import com.example.studyhub.model.Disciplina;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DisciplinaRepository extends MongoRepository<Disciplina, String> {
}
