package br.com.desafio.clientes.mensageria;

import br.com.desafio.clientes.cliente.ClienteService;
import br.com.desafio.clientes.configuracao.RabbitConfig;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
public class ConsumidorAlteracaoStatus {
    private final ClienteService clienteService;

    public ConsumidorAlteracaoStatus(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @RabbitListener(queues = RabbitConfig.FILA_ALTERACAO_STATUS)
    public void consumir(@Valid SolicitacaoAlteracaoStatus solicitacao) {
        clienteService.alterarStatusPorMensagem(solicitacao);
    }
}
