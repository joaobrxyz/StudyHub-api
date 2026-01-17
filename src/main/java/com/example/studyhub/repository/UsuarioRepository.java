package com.example.studyhub.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.studyhub.model.Usuario;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByResetToken(String token);

    List<Usuario> findByStreakAtualGreaterThanAndDataUltimaAtividadeBefore(int i, LocalDate ontem);
    List<Usuario> findByPremiumTrueAndDataFimPremiumBefore(LocalDateTime data);
}
