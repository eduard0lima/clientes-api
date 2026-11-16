package br.com.desafio.clientes.mensageria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_eventos")
public class OutboxEvento {

    @Id
    private UUID id;

    @Column(name = "tipo_evento", nullable = false, length = 80)
    private String tipoEvento;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "publicado_em")
    private Instant publicadoEm;

    protected OutboxEvento() {
    }

    public OutboxEvento(UUID id, String tipoEvento, String payload, Instant criadoEm) {
        this.id = id;
        this.tipoEvento = tipoEvento;
        this.payload = payload;
        this.criadoEm = criadoEm;
    }

    public void marcarComoPublicado() { this.publicadoEm = Instant.now(); }
    public UUID getId() { return id; }
    public String getTipoEvento() { return tipoEvento; }
    public String getPayload() { return payload; }
    public Instant getCriadoEm() { return criadoEm; }
}
