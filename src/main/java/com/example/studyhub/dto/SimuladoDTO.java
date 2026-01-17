package com.example.studyhub.dto;

import java.util.List;

import com.example.studyhub.model.Dificuldade;
import com.example.studyhub.model.Simulado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record SimuladoDTO(
        String id,

        @NotBlank(message = "O nome é obrigatório.")
        String nome,

        String descricao,

        @NotEmpty(message = "A lista de questões não pode estar vazia.")
        List<String> questoes,

        Dificuldade dificuldade
) {
        public SimuladoDTO(Simulado simulado) {
                this(simulado.getId(), simulado.getNome(), simulado.getDescricao(), simulado.getQuestoes(), simulado.getDificuldade());
        }
}