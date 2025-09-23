package com.example.studyhub.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "exames")
public class Exame {

    @Id
    private String id;
    private String nome;
    private List<String> simuladosIds; // simulados relacionados

    public Exame() {}

    public Exame(String nome, List<String> simuladosIds) {
        this.nome = nome;
        this.simuladosIds = simuladosIds;
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

    public List<String> getSimuladosIds() {
        return simuladosIds;
    }

    public void setSimuladosIds(List<String> simuladosIds) {
        this.simuladosIds = simuladosIds;
    }
}
