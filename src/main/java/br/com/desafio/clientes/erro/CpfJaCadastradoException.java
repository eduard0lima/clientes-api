package br.com.desafio.clientes.erro;

public class CpfJaCadastradoException extends RuntimeException {
    public CpfJaCadastradoException(String cpf) {
        super("Já existe um cliente cadastrado com o CPF %s".formatted(cpf));
    }
}
