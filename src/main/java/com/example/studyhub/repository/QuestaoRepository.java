package com.example.studyhub.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.studyhub.model.Questao;

public interface QuestaoRepository extends MongoRepository<Questao, String> {
}
