package com.example.studyhub.dto;

import java.time.LocalDateTime;

public record UserDTO(
        String nome,
        String email,
        long quantidadeSimulados,
        String curso,
        int streakAtual,
        boolean premium,
        LocalDateTime dataFimPremium
) {}