package com.example.studyhub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Document(collection = "questoes")
public class Questao {

    @Id
    private String id;

    @NotBlank(message = "O enunciado não pode ser vazio.")
    private String enunciado;

    @NotBlank(message = "A resposta é obrigatória.")
    private String resposta;

    @NotEmpty(message = "A questão deve ter alternativas.")
    private String[] alternativas;

    @NotBlank(message = "A disciplina não pode ser vazio.")
    private String disciplina;

    @NotNull(message = "A dificuldade é obrigatória.")
    private Dificuldade dificuldade;

    // ESTES CAMPOS SÃO OPCIONAIS E NÃO EXIGEM VALIDAÇÃO:
    private String ano;
    private String instituicao;

    public Questao() {}

    public Questao(String enunciado, String resposta, String[] alternativas, String disciplina, String ano, Dificuldade dificuldade, String instituicao) {
        this.enunciado = enunciado;
        this.resposta = resposta;
        this.alternativas = alternativas;
        this.disciplina = disciplina;
        this.ano = ano;
        this.dificuldade = dificuldade;
        this.instituicao = instituicao;
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

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public String[] getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(String[] alternativas) {
        this.alternativas = alternativas;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public Dificuldade getDificuldade() { // Alterado o tipo de retorno
        return dificuldade;
    }

    public void setDificuldade(Dificuldade dificuldade) { // Alterado o tipo do parâmetro
        this.dificuldade = dificuldade;
    }

    public String getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(String instituicao) {
        this.instituicao = instituicao;
    }
}