package com.example.studyhub.repository;

import com.example.studyhub.model.Pagamento;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PagamentoRepository extends MongoRepository<Pagamento, String> {
    // Busca o pagamento usando o ID da assinatura do Mercado Pago
    Optional<Pagamento> findByMercadoPagoId(String mercadoPagoId);
}
