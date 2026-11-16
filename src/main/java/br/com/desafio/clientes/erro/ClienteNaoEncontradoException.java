package br.com.desafio.clientes.erro;

public class ClienteNaoEncontradoException extends RuntimeException {
    public ClienteNaoEncontradoException(Long id) {
        super("Cliente %d não encontrado".formatted(id));
    }
}
