package br.com.desafio.clientes.mensageria;

import java.time.Instant;
import java.util.UUID;

public record EventoClienteCriado(
        UUID eventId,
        String eventType,
        Long customerId,
        Instant createdAt
) {
}
