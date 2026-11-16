package br.com.desafio.clientes.score;

import br.com.desafio.clientes.erro.IntegracaoScoreException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ScoreClient {

    private final RestClient restClient;

    public ScoreClient(RestClient.Builder scoreRestClientBuilder) {
        this.restClient = scoreRestClientBuilder.build();
    }

    public ScoreResponse consultar(String cpf) {
        try {
            return restClient.get().uri("/scores/{cpf}", cpf).retrieve().body(ScoreResponse.class);
        } catch (HttpClientErrorException.NotFound excecao) {
            throw new IntegracaoScoreException("Score não encontrado para o cliente", HttpStatus.NOT_FOUND.value(), excecao);
        } catch (ResourceAccessException excecao) {
            throw new IntegracaoScoreException("O serviço de score excedeu o tempo limite", HttpStatus.GATEWAY_TIMEOUT.value(), excecao);
        } catch (RestClientException excecao) {
            throw new IntegracaoScoreException("O serviço de score está indisponível", HttpStatus.BAD_GATEWAY.value(), excecao);
        }
    }
}
