package com.example.studyhub.controller;

import com.example.studyhub.dto.UserDTO;
import com.example.studyhub.model.Usuario;
import com.example.studyhub.service.SimuladoService;
import com.example.studyhub.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SimuladoService simuladoService;

    @GetMapping
    public ResponseEntity<UserDTO> getDadosUsuario() {
        Usuario usuario = usuarioService.verificarAutenticacao();

        // Conta quantos simulados o usuário tem
        long qtdSimulados = simuladoService.contarSimuladosPorUsuario(usuario.getId());

        // Mapeia para o DTO de resposta (criaremos no próximo passo)
        UserDTO response = new UserDTO(usuario.getNome(), usuario.getEmail(), qtdSimulados);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> deletarProprioUsuario() {

        Usuario usuario = usuarioService.verificarAutenticacao();

        // Chama o Service para deletar.
        usuarioService.deletarUsuario(usuario.getId());

        // Retorna mensagem de sucesso (e o cliente deve limpar o token)
        Map<String, String> response = Collections.singletonMap("message", "Usuário deletado com sucesso.");
        return ResponseEntity.ok(response);
    }
}
