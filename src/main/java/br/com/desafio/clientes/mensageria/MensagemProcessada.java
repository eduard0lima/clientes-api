package br.com.desafio.clientes.mensageria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "mensagens_processadas")
public class MensagemProcessada {
    @Id
    @Column(name = "evento_id")
    private UUID eventoId;

    @Column(name = "processado_em", nullable = false)
    private Instant processadoEm;

    protected MensagemProcessada() {
    }

    public MensagemProcessada(UUID eventoId) {
        this.eventoId = eventoId;
        this.processadoEm = Instant.now();
    }
}
