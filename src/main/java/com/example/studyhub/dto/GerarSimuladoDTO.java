package com.example.studyhub.dto;

import java.util.List;

public record GerarSimuladoDTO(
        String titulo,
        String descricao,
        int quantidade,

        // Filtros
        List<String> disciplinas,
        List<String> dificuldades,
        List<String> instituicoes,
        List<String> anos,
        Boolean apenasErros,
        Boolean comResolucao,
        Boolean comVideo
) {}