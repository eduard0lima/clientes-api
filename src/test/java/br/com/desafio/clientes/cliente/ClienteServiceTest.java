package br.com.desafio.clientes.cliente;

import br.com.desafio.clientes.cliente.dto.AtualizarClienteRequest;
import br.com.desafio.clientes.cliente.dto.CriarClienteRequest;
import br.com.desafio.clientes.erro.ClienteNaoEncontradoException;
import br.com.desafio.clientes.erro.CpfJaCadastradoException;
import br.com.desafio.clientes.mensageria.MensagemProcessadaRepository;
import br.com.desafio.clientes.mensageria.OutboxEvento;
import br.com.desafio.clientes.mensageria.OutboxEventoRepository;
import br.com.desafio.clientes.mensageria.SolicitacaoAlteracaoStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock ClienteRepository clienteRepository;
    @Mock OutboxEventoRepository outboxRepository;
    @Mock MensagemProcessadaRepository mensagemProcessadaRepository;

    ClienteService service;

    @BeforeEach
    void preparar() {
        service = new ClienteService(clienteRepository, outboxRepository,
                mensagemProcessadaRepository, new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void deveCriarClienteERegistrarEventoNaOutbox() {
        when(clienteRepository.existsByCpf("52998224725")).thenReturn(false);
        when(clienteRepository.save(any())).thenAnswer(invocacao -> {
            Cliente cliente = invocacao.getArgument(0);
            ReflectionTestUtils.setField(cliente, "id", 10L);
            return cliente;
        });

        var resposta = service.criar(new CriarClienteRequest(" Maria Silva ", "52998224725",
                "MARIA@EMAIL.COM", StatusCliente.ACTIVE));

        assertThat(resposta.id()).isEqualTo(10L);
        assertThat(resposta.nome()).isEqualTo("Maria Silva");
        assertThat(resposta.email()).isEqualTo("maria@email.com");
        ArgumentCaptor<OutboxEvento> evento = ArgumentCaptor.forClass(OutboxEvento.class);
        verify(outboxRepository).save(evento.capture());
        assertThat(evento.getValue().getPayload()).contains("CUSTOMER_CREATED", "\"customerId\":10");
    }

    @Test
    void deveRecusarCpfJaCadastrado() {
        when(clienteRepository.existsByCpf("52998224725")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new CriarClienteRequest("Maria", "52998224725",
                "maria@email.com", StatusCliente.ACTIVE)))
                .isInstanceOf(CpfJaCadastradoException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveAtualizarClienteExistente() {
        Cliente cliente = new Cliente("Nome antigo", "52998224725", "antigo@email.com", StatusCliente.ACTIVE);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        var resposta = service.atualizar(1L,
                new AtualizarClienteRequest("Nome novo", "NOVO@EMAIL.COM", StatusCliente.INACTIVE));

        assertThat(resposta.nome()).isEqualTo("Nome novo");
        assertThat(resposta.email()).isEqualTo("novo@email.com");
        assertThat(resposta.status()).isEqualTo(StatusCliente.INACTIVE);
    }

    @Test
    void deveInformarQuandoClienteNaoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(99L)).isInstanceOf(ClienteNaoEncontradoException.class);
    }

    @Test
    void deveIgnorarMensagemJaProcessada() {
        UUID eventoId = UUID.randomUUID();
        when(mensagemProcessadaRepository.existsById(eventoId)).thenReturn(true);

        service.alterarStatusPorMensagem(new SolicitacaoAlteracaoStatus(eventoId,
                "CUSTOMER_STATUS_CHANGE", 1L, StatusCliente.INACTIVE));

        verify(clienteRepository, never()).findById(any());
    }
}
