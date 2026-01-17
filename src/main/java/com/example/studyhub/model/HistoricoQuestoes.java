package com.example.studyhub.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "historico_questoes")
@CompoundIndex(name = "user_quest_idx", def = "{'usuarioId': 1, 'questaoId': 1}", unique = true)
public class HistoricoQuestoes {

    @Id
    private String id;

    @NotBlank(message = "O id do usuário não pode ser vazio.")
    private String usuarioId;
    @NotBlank(message = "O id da questão não pode ser vazio.")
    private String questaoId;

    private String statusAtual;

    private int tentativas;
    private int erros;
    private int acertos;

    private LocalDateTime ultimaResolucao;

    private List<String> topicos;

    public HistoricoQuestoes() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public String getQuestaoId() { return questaoId; }
    public void setQuestaoId(String questaoId) { this.questaoId = questaoId; }

    public String getStatusAtual() {
        return statusAtual;
    }

    public void setStatusAtual(String statusAtual) {
        this.statusAtual = statusAtual;
    }

    public int getTentativas() {
        return tentativas;
    }

    public void setTentativas(int tentativas) {
        this.tentativas = tentativas;
    }

    public int getErros() {
        return erros;
    }

    public void setErros(int erros) {
        this.erros = erros;
    }

    public int getAcertos() {
        return acertos;
    }

    public void setAcertos(int acertos) {
        this.acertos = acertos;
    }

    public LocalDateTime getUltimaResolucao() {
        return ultimaResolucao;
    }

    public void setUltimaResolucao(LocalDateTime ultimaResolucao) {
        this.ultimaResolucao = ultimaResolucao;
    }

    public List<String> getTopicos() {
        return topicos;
    }

    public void setTopicos(List<String> topicos) {
        this.topicos = topicos;
    }
}