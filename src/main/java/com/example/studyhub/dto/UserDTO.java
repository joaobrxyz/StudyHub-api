package com.example.studyhub.dto;

public record UserDTO(
        String nome,
        String email,
        long quantidadeSimulados
) {}