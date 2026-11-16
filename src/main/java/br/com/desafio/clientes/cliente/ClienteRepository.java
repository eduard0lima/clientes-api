package br.com.desafio.clientes.cliente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCpf(String cpf);

    Page<Cliente> findByStatus(StatusCliente status, Pageable pageable);

    List<Cliente> findTop20ByNomeContainingIgnoreCaseOrderByNome(String nome);
}
