package com.example.studyhub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "questoes")
public class Questao {

    @Id
    private String id;
    private String enunciado;
    private String respostaCorreta;
    private String[] alternativas;
    private String disciplinaId; // referência à disciplina

    public Questao() {}

    public Questao(String enunciado, String respostaCorreta, String[] alternativas, String disciplinaId) {
        this.enunciado = enunciado;
        this.respostaCorreta = respostaCorreta;
        this.alternativas = alternativas;
        this.disciplinaId = disciplinaId;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getRespostaCorreta() {
        return respostaCorreta;
    }

    public void setRespostaCorreta(String respostaCorreta) {
        this.respostaCorreta = respostaCorreta;
    }

    public String[] getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(String[] alternativas) {
        this.alternativas = alternativas;
    }

    public String getDisciplinaId() {
        return disciplinaId;
    }

    public void setDisciplinaId(String disciplinaId) {
        this.disciplinaId = disciplinaId;
    }
}
