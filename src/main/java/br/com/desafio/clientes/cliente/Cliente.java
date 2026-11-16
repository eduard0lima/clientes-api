package br.com.desafio.clientes.cliente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 254)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCliente status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Version
    private long versao;

    protected Cliente() {
    }

    public Cliente(String nome, String cpf, String email, StatusCliente status) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.status = status;
        this.criadoEm = Instant.now();
        this.atualizadoEm = this.criadoEm;
    }

    public void atualizar(String nome, String email, StatusCliente status) {
        this.nome = nome;
        this.email = email;
        this.status = status;
        this.atualizadoEm = Instant.now();
    }

    public void alterarStatus(StatusCliente status) {
        this.status = status;
        this.atualizadoEm = Instant.now();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
    public StatusCliente getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
