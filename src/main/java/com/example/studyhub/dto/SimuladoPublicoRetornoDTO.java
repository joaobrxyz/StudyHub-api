package com.example.studyhub.dto;

import com.example.studyhub.model.Dificuldade;
import com.example.studyhub.model.Simulado;

import java.time.LocalDateTime;
import java.util.List;

public record SimuladoPublicoRetornoDTO(
        String id,
        String nome,
        String descricao,
        LocalDateTime data,
        List<String> questoes,
        Dificuldade dificuldade
) {
    public SimuladoPublicoRetornoDTO(Simulado simulado){
        this(simulado.getId(), simulado.getNome(), simulado.getDescricao(), simulado.getData(), simulado.getQuestoes(), simulado.getDificuldade());
    }
}
