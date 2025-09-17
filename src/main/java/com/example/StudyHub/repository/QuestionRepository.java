package com.example.StudyHub.repository;

import com.example.StudyHub.model.Question;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuestionRepository extends MongoRepository<Question, String> {

}
