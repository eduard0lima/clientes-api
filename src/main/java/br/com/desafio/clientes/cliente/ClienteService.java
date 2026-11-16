package br.com.desafio.clientes.cliente;

import br.com.desafio.clientes.cliente.dto.AtualizarClienteRequest;
import br.com.desafio.clientes.cliente.dto.ClienteResponse;
import br.com.desafio.clientes.cliente.dto.CriarClienteRequest;
import br.com.desafio.clientes.erro.ClienteNaoEncontradoException;
import br.com.desafio.clientes.erro.CpfJaCadastradoException;
import br.com.desafio.clientes.mensageria.EventoClienteCriado;
import br.com.desafio.clientes.mensageria.MensagemProcessada;
import br.com.desafio.clientes.mensageria.MensagemProcessadaRepository;
import br.com.desafio.clientes.mensageria.OutboxEvento;
import br.com.desafio.clientes.mensageria.OutboxEventoRepository;
import br.com.desafio.clientes.mensageria.SolicitacaoAlteracaoStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final OutboxEventoRepository outboxRepository;
    private final MensagemProcessadaRepository mensagemProcessadaRepository;
    private final ObjectMapper objectMapper;

    public ClienteService(ClienteRepository clienteRepository, OutboxEventoRepository outboxRepository,
                          MensagemProcessadaRepository mensagemProcessadaRepository, ObjectMapper objectMapper) {
        this.clienteRepository = clienteRepository;
        this.outboxRepository = outboxRepository;
        this.mensagemProcessadaRepository = mensagemProcessadaRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ClienteResponse criar(CriarClienteRequest request) {
        if (clienteRepository.existsByCpf(request.cpf())) {
            throw new CpfJaCadastradoException(request.cpf());
        }

        Cliente cliente = clienteRepository.save(new Cliente(request.nome().trim(), request.cpf(),
                request.email().trim().toLowerCase(), request.status()));
        registrarEventoDeCriacao(cliente);
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public ClienteResponse atualizar(Long id, AtualizarClienteRequest request) {
        Cliente cliente = buscarEntidade(id);
        cliente.atualizar(request.nome().trim(), request.email().trim().toLowerCase(), request.status());
        return ClienteResponse.de(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarEntidade(id);
        clienteRepository.delete(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long id) {
        return ClienteResponse.de(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listar(StatusCliente status, Pageable pageable) {
        Page<Cliente> clientes = status == null
                ? clienteRepository.findAll(pageable)
                : clienteRepository.findByStatus(status, pageable);
        return clientes.map(ClienteResponse::de);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarPorNome(String nome) {
        return clienteRepository.findTop20ByNomeContainingIgnoreCaseOrderByNome(nome.trim()).stream()
                .map(ClienteResponse::de)
                .toList();
    }

    @Transactional
    public void alterarStatusPorMensagem(SolicitacaoAlteracaoStatus solicitacao) {
        if (mensagemProcessadaRepository.existsById(solicitacao.eventId())) {
            return;
        }
        if (!"CUSTOMER_STATUS_CHANGE".equals(solicitacao.eventType())) {
            throw new IllegalArgumentException("Tipo de evento não suportado: " + solicitacao.eventType());
        }
        Cliente cliente = buscarEntidade(solicitacao.customerId());
        cliente.alterarStatus(solicitacao.status());
        mensagemProcessadaRepository.save(new MensagemProcessada(solicitacao.eventId()));
    }

    private void registrarEventoDeCriacao(Cliente cliente) {
        UUID eventoId = UUID.randomUUID();
        Instant agora = Instant.now();
        EventoClienteCriado evento = new EventoClienteCriado(eventoId, "CUSTOMER_CREATED", cliente.getId(), agora);
        try {
            outboxRepository.save(new OutboxEvento(eventoId, evento.eventType(), objectMapper.writeValueAsString(evento), agora));
        } catch (JsonProcessingException excecao) {
            throw new IllegalStateException("Não foi possível serializar o evento de criação", excecao);
        }
    }
}
