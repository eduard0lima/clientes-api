package br.com.desafio.clientes.cliente.dto;

import br.com.desafio.clientes.cliente.StatusCliente;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record CriarClienteRequest(
        @JsonProperty("name") @NotBlank @Size(max = 150) String nome,
        @CPF @NotBlank String cpf,
        @Email @NotBlank @Size(max = 254) String email,
        @NotNull StatusCliente status
) {
}
