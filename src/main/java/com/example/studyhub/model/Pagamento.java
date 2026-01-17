package com.example.studyhub.model;

import com.mercadopago.resources.payment.PaymentMethod;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "pagamentos")
public class Pagamento {

    @Id
    private String id;
    private String usuarioId;
    private String mercadoPagoId;
    private String status;
    private String formaPagamento;
    private LocalDateTime dataAtualizacao;

    public Pagamento() {}

    public Pagamento(String usuarioId, String mercadoPagoId, String status, String formaPagamento) {
        this.usuarioId = usuarioId;
        this.mercadoPagoId = mercadoPagoId;
        this.status = status;
        this.formaPagamento = formaPagamento;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getMercadoPagoId() { return mercadoPagoId; }
    public void setMercadoPagoId(String mercadoPagoId) { this.mercadoPagoId = mercadoPagoId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}