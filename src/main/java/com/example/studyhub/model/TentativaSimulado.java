package com.example.studyhub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "tentativas_simulados")
public class TentativaSimulado {

    @Id
    private String id;
    private String usuarioId; // Relaciona ao usuário logado
    private String simuladoId; // Relaciona ao simulado feito
    private String nomeSimulado;
    private LocalDateTime dataFim;
    private Long tempoTotalSegundos;
    private Integer totalAcertos;
    private Integer totalQuestoes;
    private Double porcentagemAcerto;
    private Dificuldade dificuldadeSimulado; // Enum de dificuldade
    private List<RespostaDetalhe> detalhesRespostas; // Detalhamento de cada questão
    private String idTentativaOriginal; // Se for null, é um simulado completo. Se tiver ID, é um "Refazer Erros".
    private boolean apenasErros; // Flag para facilitar filtros rápidos

    // Construtor Vazio
    public TentativaSimulado() {}

    // Construtor Completo
    public TentativaSimulado(String id, String usuarioId, String simuladoId, String nomeSimulado,
                             LocalDateTime dataFim, Long tempoTotalSegundos, Integer totalAcertos,
                             Integer totalQuestoes, Double porcentagemAcerto, Dificuldade dificuldadeSimulado,
                             List<RespostaDetalhe> detalhesRespostas) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.simuladoId = simuladoId;
        this.nomeSimulado = nomeSimulado;
        this.dataFim = dataFim;
        this.tempoTotalSegundos = tempoTotalSegundos;
        this.totalAcertos = totalAcertos;
        this.totalQuestoes = totalQuestoes;
        this.porcentagemAcerto = porcentagemAcerto;
        this.dificuldadeSimulado = dificuldadeSimulado;
        this.detalhesRespostas = detalhesRespostas;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getSimuladoId() { return simuladoId; }
    public void setSimuladoId(String simuladoId) { this.simuladoId = simuladoId; }

    public String getNomeSimulado() { return nomeSimulado; }
    public void setNomeSimulado(String nomeSimulado) { this.nomeSimulado = nomeSimulado; }

    public LocalDateTime getDataFim() { return dataFim; }
    public void setDataFim(LocalDateTime dataFim) { this.dataFim = dataFim; }

    public Long getTempoTotalSegundos() { return tempoTotalSegundos; }
    public void setTempoTotalSegundos(Long tempoTotalSegundos) { this.tempoTotalSegundos = tempoTotalSegundos; }

    public Integer getTotalAcertos() { return totalAcertos; }
    public void setTotalAcertos(Integer totalAcertos) { this.totalAcertos = totalAcertos; }

    public Integer getTotalQuestoes() { return totalQuestoes; }
    public void setTotalQuestoes(Integer totalQuestoes) { this.totalQuestoes = totalQuestoes; }

    public Double getPorcentagemAcerto() { return porcentagemAcerto; }
    public void setPorcentagemAcerto(Double porcentagemAcerto) { this.porcentagemAcerto = porcentagemAcerto; }

    public Dificuldade getDificuldadeSimulado() { return dificuldadeSimulado; }
    public void setDificuldadeSimulado(Dificuldade dificuldadeSimulado) { this.dificuldadeSimulado = dificuldadeSimulado; }

    public List<RespostaDetalhe> getDetalhesRespostas() { return detalhesRespostas; }
    public void setDetalhesRespostas(List<RespostaDetalhe> detalhesRespostas) { this.detalhesRespostas = detalhesRespostas; }

    public String getIdTentativaOriginal() {
        return idTentativaOriginal;
    }

    public void setIdTentativaOriginal(String idTentativaOriginal) {
        this.idTentativaOriginal = idTentativaOriginal;
    }

    public boolean isApenasErros() {
        return apenasErros;
    }

    public void setApenasErros(boolean apenasErros) {
        this.apenasErros = apenasErros;
    }
}