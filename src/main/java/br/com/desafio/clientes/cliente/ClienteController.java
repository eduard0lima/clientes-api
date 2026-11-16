package br.com.desafio.clientes.cliente;

import br.com.desafio.clientes.cliente.dto.AtualizarClienteRequest;
import br.com.desafio.clientes.cliente.dto.ClienteResponse;
import br.com.desafio.clientes.cliente.dto.CriarClienteRequest;
import br.com.desafio.clientes.score.ScoreClient;
import br.com.desafio.clientes.score.ScoreResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/customers")
public class ClienteController {

    private final ClienteService clienteService;
    private final ScoreClient scoreClient;

    public ClienteController(ClienteService clienteService, ScoreClient scoreClient) {
        this.clienteService = clienteService;
        this.scoreClient = scoreClient;
    }

    @PostMapping
    ResponseEntity<ClienteResponse> criar(@Valid @RequestBody CriarClienteRequest request) {
        ClienteResponse resposta = clienteService.criar(request);
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(localizacao).body(resposta);
    }

    @PutMapping("/{id}")
    ClienteResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarClienteRequest request) {
        return clienteService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void excluir(@PathVariable Long id) {
        clienteService.excluir(id);
    }

    @GetMapping("/{id}")
    ClienteResponse buscar(@PathVariable Long id) {
        return clienteService.buscar(id);
    }

    @GetMapping
    Page<ClienteResponse> listar(@RequestParam(required = false) StatusCliente status,
                                 @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return clienteService.listar(status, pageable);
    }

    @GetMapping("/search")
    List<ClienteResponse> buscarPorNome(@RequestParam @NotBlank String name) {
        return clienteService.buscarPorNome(name);
    }

    @GetMapping("/{id}/score")
    ScoreResponse consultarScore(@PathVariable Long id) {
        Cliente cliente = clienteService.buscarEntidade(id);
        return scoreClient.consultar(cliente.getCpf());
    }
}
