package com.example.studyhub.controller;

import com.example.studyhub.model.Usuario;
import com.example.studyhub.service.PagamentoService;
import com.example.studyhub.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/assinar")
    public ResponseEntity<String> assinar() {
        // 1. Pega o utilizador logado
        Usuario usuario = usuarioService.verificarAutenticacao();

        // 2. Gera o link do Mercado Pago
        String urlCheckout = pagamentoService.criarAssinatura(usuario);

        // 3. Devolve o link para o Angular abrir
        return ResponseEntity.ok(urlCheckout);
    }
}
