package com.example.studyhub.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "simulados")
public class Simulado {

    @Id
    private String id;
    private String usuarioId; // referência ao usuário
    private List<String> questoesIds; // lista de questões
    private int pontuacao;

    public Simulado() {}

    public Simulado(String usuarioId, List<String> questoesIds, int pontuacao) {
        this.usuarioId = usuarioId;
        this.questoesIds = questoesIds;
        this.pontuacao = pontuacao;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<String> getQuestoesIds() {
        return questoesIds;
    }

    public void setQuestoesIds(List<String> questoesIds) {
        this.questoesIds = questoesIds;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(int pontuacao) {
        this.pontuacao = pontuacao;
    }
}
