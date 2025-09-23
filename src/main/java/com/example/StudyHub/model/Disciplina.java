package com.example.studyhub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "disciplinas")
public class Disciplina {

    @Id
    private String id;
    private String nome;

    public Disciplina() {}

    public Disciplina(String nome) {
        this.nome = nome;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
