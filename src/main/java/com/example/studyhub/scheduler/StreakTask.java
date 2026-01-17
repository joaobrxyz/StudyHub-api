package com.example.studyhub.scheduler;

import com.example.studyhub.model.Usuario;
import com.example.studyhub.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class StreakTask {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Roda todo dia às 00:00:01
    // Cron: segundos, minutos, horas, dia, mês, dia da semana
    @Scheduled(cron = "1 0 0 * * *")
    public void resetarStreaksExpirados() {
        LocalDate ontem = LocalDate.now().minusDays(1);

        // Buscamos usuários que:
        // 1. Tem streak > 0
        // 2. A última atividade foi ANTES de ontem
        List<Usuario> usuariosParaResetar = usuarioRepository
                .findByStreakAtualGreaterThanAndDataUltimaAtividadeBefore(0, ontem);

        usuariosParaResetar.forEach(u -> {
            u.setStreakAtual(0);
        });

        usuarioRepository.saveAll(usuariosParaResetar);
        System.out.println("Streaks resetados para " + usuariosParaResetar.size() + " usuários.");
    }
}
