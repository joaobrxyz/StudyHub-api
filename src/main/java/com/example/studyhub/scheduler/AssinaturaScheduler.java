package com.example.studyhub.scheduler;

import com.example.studyhub.model.Usuario;
import com.example.studyhub.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class AssinaturaScheduler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Roda todos os dias à meia-noite (cron = "segundo minuto hora dia mês dia-da-semana")
    @Scheduled(cron = "0 0 0 * * *")
    public void verificarAssinaturasExpiradas() {
        LocalDateTime agora = LocalDateTime.now();

        // Procura usuários que são Premium e a data de validade é menor que 'agora'
        List<Usuario> expirados = usuarioRepository.findByPremiumTrueAndDataFimPremiumBefore(agora);

        if (!expirados.isEmpty()) {
            for (Usuario u : expirados) {
                u.setPremium(false); // Volta para o plano básico
                usuarioRepository.save(u);
                System.out.println("LOG: Premium expirado para o utilizador: " + u.getEmail());
            }
            System.out.println("LOG: Total de " + expirados.size() + " assinaturas encerradas hoje.");
        }
    }
}
