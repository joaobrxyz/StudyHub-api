package com.example.studyhub.dto;

import com.example.studyhub.model.Dificuldade;
import com.example.studyhub.model.Simulado;

import java.time.LocalDateTime;
import java.util.List;

public record SimuladoRetornoDTO(
        String id,
        String nome,
        String descricao,
        LocalDateTime data,
        int quantidadeQuestoes,
        Dificuldade dificuldade
) {
    public SimuladoRetornoDTO(Simulado simulado){
        this(simulado.getId(), simulado.getNome(), simulado.getDescricao(), simulado.getData(),  simulado.getQuestoes() != null ? simulado.getQuestoes().size() : 0, simulado.getDificuldade());
    }
}
