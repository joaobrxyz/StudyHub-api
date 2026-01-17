package com.example.studyhub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.time.LocalDateTime;

@Document(collection = "simulados")
public class Simulado {

    @Id
    private String id;

    @NotBlank(message = "O nome do simulado é obrigatório.")
    private String nome;

    private String descricao; // Opcional

    @NotNull(message = "A data de criação é obrigatória.")
    private LocalDateTime data;

    @NotBlank(message = "O ID do usuário é obrigatório.")
    private String idUser;

    @NotEmpty(message = "O simulado deve ter questões.")
    private List<String> questoes;

    private boolean publico = false;

    private Dificuldade dificuldade;

    public Simulado() {
        this.data = LocalDateTime.now(); // Define a data de criação automaticamente
    }

    public Simulado(String nome, String descricao, String idUser, List<String> questoes, boolean publico, Dificuldade dificuldade) {
        this.nome = nome;
        this.descricao = descricao;
        this.data = LocalDateTime.now();
        this.idUser = idUser;
        this.questoes = questoes;
        this.publico = publico;
        this.dificuldade = dificuldade;
    }

    // --- Getters e Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getData() { return data; }
    // A data será definida automaticamente, mas mantemos o setter por boas práticas do Spring
    public void setData(LocalDateTime data) { this.data = data; }

    public String getIdUser() { return idUser; }
    public void setIdUser(String idUser) { this.idUser = idUser; }

    public List<String> getQuestoes() { return questoes; }
    public void setQuestoes(List<String> questoes) { this.questoes = questoes; }

    public boolean isPublico() {
        return publico;
    }

    public void setPublico(boolean publico) {
        this.publico = publico;
    }

    public Dificuldade getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(Dificuldade dificuldade) {
        this.dificuldade = dificuldade;
    }
}