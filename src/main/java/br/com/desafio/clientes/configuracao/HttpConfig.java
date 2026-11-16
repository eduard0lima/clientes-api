package br.com.desafio.clientes.configuracao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class HttpConfig {

    @Bean
    RestClient.Builder scoreRestClientBuilder(
            @Value("${integracoes.score.url}") String url,
            @Value("${integracoes.score.connect-timeout:2s}") Duration connectTimeout,
            @Value("${integracoes.score.read-timeout:2s}") Duration readTimeout) {
        ClientHttpRequestFactorySettings configuracoes = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(connectTimeout)
                .withReadTimeout(readTimeout);
        return RestClient.builder()
                .baseUrl(url)
                .requestFactory(ClientHttpRequestFactoryBuilder.detect().build(configuracoes));
    }
}
