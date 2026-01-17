package com.example.studyhub.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;
    @NotBlank(message = "O nome não pode ser vazio.")
    private String nome;
    @NotBlank(message = "O email não pode ser vazio.")
    private String email;
    private String senha;
    private String role = "USER";
    private String curso;
    private boolean premium = false;
    private LocalDateTime dataFimPremium;

    private String resetToken;
    private LocalDateTime tokenExpiryDate;
    private int streakAtual = 0;
    private LocalDate dataUltimaAtividade;
    private int simuladosAutomaticosRestantes = 2;
    private String mesReferenciaLimite;

    public Usuario() {
    }

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getRole() {return role;}

    public void setRole(String role) {this.role = role;}

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getTokenExpiryDate() {
        return tokenExpiryDate;
    }

    public void setTokenExpiryDate(LocalDateTime tokenExpiryDate) {
        this.tokenExpiryDate = tokenExpiryDate;
    }

    public int getStreakAtual() {
        return streakAtual;
    }

    public void setStreakAtual(int streakAtual) {
        this.streakAtual = streakAtual;
    }

    public LocalDate getDataUltimaAtividade() {
        return dataUltimaAtividade;
    }

    public void setDataUltimaAtividade(LocalDate dataUltimaAtividade) {
        this.dataUltimaAtividade = dataUltimaAtividade;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public int getSimuladosAutomaticosRestantes() {
        return simuladosAutomaticosRestantes;
    }

    public void setSimuladosAutomaticosRestantes(int simuladosAutomaticosRestantes) {
        this.simuladosAutomaticosRestantes = simuladosAutomaticosRestantes;
    }

    public String getMesReferenciaLimite() {
        return mesReferenciaLimite;
    }

    public void setMesReferenciaLimite(String mesReferenciaLimite) {
        this.mesReferenciaLimite = mesReferenciaLimite;
    }

    public LocalDateTime getDataFimPremium() {
        return dataFimPremium;
    }

    public void setDataFimPremium(LocalDateTime dataFimPremium) {
        this.dataFimPremium = dataFimPremium;
    }
}
