package br.com.desafio.clientes.cliente;

import br.com.desafio.clientes.cliente.dto.ClienteResponse;
import br.com.desafio.clientes.configuracao.SegurancaConfig;
import br.com.desafio.clientes.configuracao.FusoHorarioConfig;
import br.com.desafio.clientes.score.ScoreClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ClienteController.class)
@Import({SegurancaConfig.class, FusoHorarioConfig.class})
class ClienteControllerSegurancaTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ClienteService clienteService;
    @MockitoBean ScoreClient scoreClient;

    @Test
    void usuarioPodeConsultar() throws Exception {
        when(clienteService.buscar(1L)).thenReturn(new ClienteResponse(1L, "Maria", "52998224725",
                "maria@email.com", StatusCliente.ACTIVE, Instant.parse("2026-11-11T15:30:00Z"),
                Instant.parse("2026-11-11T15:30:00Z")));

        mockMvc.perform(get("/customers/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt").value("2026-11-11T12:30:00-03:00"))
                .andExpect(jsonPath("$.updatedAt").value("2026-11-11T12:30:00-03:00"));
    }

    @Test
    void usuarioNaoPodeCadastrar() throws Exception {
        mockMvc.perform(post("/customers")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType("application/json")
                        .content("""
                                {"name":"Maria","cpf":"52998224725","email":"maria@email.com","status":"ACTIVE"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void requisicaoSemTokenDeveSerRecusada() throws Exception {
        mockMvc.perform(get("/customers/1")).andExpect(status().isUnauthorized());
    }
}
