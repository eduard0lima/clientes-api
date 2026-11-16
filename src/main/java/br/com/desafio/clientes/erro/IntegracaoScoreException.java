package br.com.desafio.clientes.erro;

public class IntegracaoScoreException extends RuntimeException {
    private final int statusHttp;

    public IntegracaoScoreException(String mensagem, int statusHttp, Throwable causa) {
        super(mensagem, causa);
        this.statusHttp = statusHttp;
    }

    public int getStatusHttp() {
        return statusHttp;
    }
}
