package com.example.studyhub.dto;

import java.util.List;

public record RegistrarTentativaDTO(
        String questaoId,
        boolean acertou,
        List<String> topicos
) {}