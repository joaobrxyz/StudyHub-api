package com.example.studyhub.model;

public class RespostaDetalhe {
    private String questaoId;
    private String alternativaMarcada;
    private boolean correta;
    private Long tempoGastoSegundos;

    public RespostaDetalhe() {}

    public RespostaDetalhe(String questaoId, String alternativaMarcada, boolean correta, Long tempoGastoSegundos) {
        this.questaoId = questaoId;
        this.alternativaMarcada = alternativaMarcada;
        this.correta = correta;
        this.tempoGastoSegundos = tempoGastoSegundos;
    }

    // Getters e Setters
    public String getQuestaoId() { return questaoId; }
    public void setQuestaoId(String questaoId) { this.questaoId = questaoId; }

    public String getAlternativaMarcada() { return alternativaMarcada; }
    public void setAlternativaMarcada(String alternativaMarcada) { this.alternativaMarcada = alternativaMarcada; }

    public boolean isCorreta() { return correta; }
    public void setCorreta(boolean correta) { this.correta = correta; }

    public Long getTempoGastoSegundos() { return tempoGastoSegundos; }
    public void setTempoGastoSegundos(Long tempoGastoSegundos) { this.tempoGastoSegundos = tempoGastoSegundos; }
}