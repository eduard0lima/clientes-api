package br.com.desafio.clientes.cliente.dto;

import br.com.desafio.clientes.cliente.Cliente;
import br.com.desafio.clientes.cliente.StatusCliente;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record ClienteResponse(
        Long id,
        @JsonProperty("name") String nome,
        String cpf,
        String email,
        StatusCliente status,
        @JsonProperty("createdAt") Instant criadoEm,
        @JsonProperty("updatedAt") Instant atualizadoEm
) {
    public static ClienteResponse de(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getCpf(), cliente.getEmail(),
                cliente.getStatus(), cliente.getCriadoEm(), cliente.getAtualizadoEm());
    }
}
