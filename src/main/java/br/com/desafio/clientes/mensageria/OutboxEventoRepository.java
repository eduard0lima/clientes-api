package br.com.desafio.clientes.mensageria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventoRepository extends JpaRepository<OutboxEvento, UUID> {
    List<OutboxEvento> findTop50ByPublicadoEmIsNullOrderByCriadoEmAsc();
}
