package com.example.studyhub.repository;

import com.example.studyhub.model.Exame;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExameRepository extends MongoRepository<Exame, String> {
}

