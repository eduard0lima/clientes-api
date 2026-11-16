package br.com.desafio.clientes.mensageria;

import br.com.desafio.clientes.cliente.StatusCliente;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SolicitacaoAlteracaoStatus(
        @NotNull UUID eventId,
        @NotNull String eventType,
        @NotNull Long customerId,
        @NotNull StatusCliente status
) {
}
