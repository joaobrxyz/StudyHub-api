package com.example.studyhub.controller;

import com.example.studyhub.model.Pagamento;
import com.example.studyhub.model.Usuario;
import com.example.studyhub.repository.PagamentoRepository;
import com.example.studyhub.repository.UsuarioRepository;
import com.example.studyhub.service.PagamentoService; // Sugestão: usar o service
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment; // Import correto do recurso
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @PostMapping("/mercado-pago")
    public ResponseEntity<Void> receberNotificacao(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Identifica o tipo de aviso (O MP envia 'payment' para PIX/Cartão)
            String type = (String) payload.get("type");

            if ("payment".equals(type)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                String paymentId = String.valueOf(data.get("id"));

                // Tratamento para o simulador não dar erro 500
                if ("123456".equals(paymentId)) {
                    System.out.println("LOG: Teste do simulador recebido com sucesso!");
                    return ResponseEntity.ok().build();
                }

                processarPagamento(paymentId);
            }

            return ResponseEntity.ok().build(); // Sempre retorne 200 para o MP parar de tentar
        } catch (Exception e) {
            System.err.println("Erro no Webhook: " + e.getMessage());
            return ResponseEntity.status(200).build(); // Retornamos 200 mesmo no erro para evitar loop de tentativas do MP
        }
    }

    public void processarPagamento(String paymentId) throws Exception {
        // Token de Produção
        MercadoPagoConfig.setAccessToken(accessToken);

        PaymentClient client = new PaymentClient();
        Payment payment = client.get(Long.parseLong(paymentId)); // Busca os dados reais na API

        // Verifica se o status é aprovado e se temos o ID do usuário
        if ("approved".equals(payment.getStatus()) && payment.getExternalReference() != null) {
            String usuarioId = payment.getExternalReference();

            // Salva o registro do pagamento no seu histórico
            // Usei getPaymentMethodId() que é o padrão do SDK do Mercado Pago
            Pagamento pagamento = new Pagamento(usuarioId, paymentId, payment.getStatus(), payment.getPaymentMethodId());
            pagamentoRepository.save(pagamento);

            // Busca o usuário e ativa o Premium
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + usuarioId));
            LocalDateTime dataAtual = LocalDateTime.now();
            LocalDateTime novaDataExpiracao;

            // Lógica de Soma:
            if (usuario.getDataFimPremium() != null && usuario.getDataFimPremium().isAfter(dataAtual)) {
                // Se ele já é PRO e ainda tem dias, soma 30 dias à data que ele já tinha
                novaDataExpiracao = usuario.getDataFimPremium().plusDays(30);
            } else {
                // Se ele é Básico ou o Premium já venceu, soma 30 dias a partir de agora
                novaDataExpiracao = dataAtual.plusDays(30);
            }

            usuario.setDataFimPremium(novaDataExpiracao);
            usuario.setPremium(true);
            usuarioRepository.save(usuario);

            System.out.println("SUCESSO: StudyHub Premium Ativado para: " + usuario.getEmail());
        }
    }
}