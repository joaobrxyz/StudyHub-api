package com.example.studyhub.service;

import com.example.studyhub.model.Simulado;
import com.example.studyhub.repository.SimuladoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SimuladoService {
    @Autowired
    private SimuladoRepository repository;

    public List<Simulado> listarPorUsuario(String idUser) {
        // Usa o método do Spring Data que filtra pelo campo 'idUser'
        return repository.findByIdUser(idUser);
    }

    public Simulado criar(Simulado simulado) {
        return repository.save(simulado);
    }

    public Optional<Simulado> buscarPorIdEUsuario(String simuladoId, String idUser) {
        return repository.findByIdAndIdUser(simuladoId, idUser);
    }

    public boolean deletarPorId(String simuladoId, String idUser) {

        // 1. Busca o simulado pelo ID
        Optional<Simulado> simuladoOpt = repository.findById(simuladoId);

        if (simuladoOpt.isEmpty()) {
            return false; // Não encontrado
        }

        Simulado simulado = simuladoOpt.get();

        // 2. Verifica se o usuário logado é o dono (dono do simulado == usuário logado)
        if (simulado.getIdUser().equals(idUser)) {
            repository.deleteById(simuladoId); // Exclui
            return true; // Sucesso
        } else {
            // O simulado existe, mas o usuário logado NÃO é o dono.
            // Retornamos false, e o Controller interpreta como 404 (para não expor a existência do simulado).
            return false;
        }
    }

    public long contarSimuladosPorUsuario(String idUser) {
        return repository.countByIdUser(idUser);
    }
}
