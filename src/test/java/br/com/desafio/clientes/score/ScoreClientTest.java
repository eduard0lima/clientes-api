package br.com.desafio.clientes.score;

import br.com.desafio.clientes.erro.IntegracaoScoreException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ScoreClientTest {

    @Test
    void deveConsultarScore() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://score");
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(once(), requestTo("http://score/scores/52998224725"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"cpf":"52998224725","score":750,"classification":"LOW_RISK"}
                        """, org.springframework.http.MediaType.APPLICATION_JSON));

        ScoreResponse resposta = new ScoreClient(builder).consultar("52998224725");

        assertThat(resposta.score()).isEqualTo(750);
        assertThat(resposta.classification()).isEqualTo("LOW_RISK");
        servidor.verify();
    }

    @Test
    void deveTraduzirScoreInexistente() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://score");
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo("http://score/scores/52998224725")).andRespond(withResourceNotFound());

        assertThatThrownBy(() -> new ScoreClient(builder).consultar("52998224725"))
                .isInstanceOf(IntegracaoScoreException.class)
                .extracting("statusHttp").isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
