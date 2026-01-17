package com.example.studyhub.service;

import com.example.studyhub.model.Usuario;
import com.example.studyhub.model.Pagamento;
import com.example.studyhub.repository.PagamentoRepository;
import com.example.studyhub.repository.UsuarioRepository; // Import necessário
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${mercadopago.access.token}")
    private String accessToken;

    public String criarAssinatura(Usuario usuario) {
        try {
            // Token de Produção
            MercadoPagoConfig.setAccessToken(accessToken);

            // 1. Configuração do Item (R$ 9,90)
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("StudyHub Premium - Mensal")
                    .quantity(1)
                    .unitPrice(new BigDecimal("9.90"))
                    .currencyId("BRL")
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(item);

            // 2. Filtro de Meios de Pagamento (Apenas PIX, Cartão e Saldo)
            List<PreferencePaymentTypeRequest> excludedTypes = new ArrayList<>();
            excludedTypes.add(PreferencePaymentTypeRequest.builder().id("ticket").build()); // Remove Boleto
            excludedTypes.add(PreferencePaymentTypeRequest.builder().id("debit_card").build()); // Remove Débito

            PreferencePaymentMethodsRequest paymentMethods = PreferencePaymentMethodsRequest.builder()
                    .excludedPaymentTypes(excludedTypes)
                    .installments(12)
                    .build();

            // 3. Configuração de Redirecionamento (Para não travar no QR Code)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("https://studyhub.com.br/premium")
                    .failure("https://studyhub.com.br/premium")
                    .pending("https://studyhub.com.br/premium")
                    .build();

            // 4. Montando a Preferência Final
            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .payer(PreferencePayerRequest.builder().email(usuario.getEmail()).build())
                    .externalReference(usuario.getId()) // ID do aluno para o Webhook
                    .paymentMethods(paymentMethods)
                    .backUrls(backUrls) // Define os links de retorno
                    .autoReturn("approved") // Faz o redirecionamento automático após aprovação
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(request);

            return preference.getInitPoint();

        } catch (MPApiException e) {
            System.err.println("DADOS DO ERRO DO MERCADO PAGO: " + e.getApiResponse().getContent());
            throw new RuntimeException("Erro na API do Mercado Pago: " + e.getMessage());
        } catch (MPException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro de conexão no SDK: " + e.getMessage());
        }
    }
}