package br.com.desafio.clientes.erro;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class TratadorGlobalErros {

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    ProblemDetail tratarClienteNaoEncontrado(ClienteNaoEncontradoException excecao) {
        return problema(HttpStatus.NOT_FOUND, "Cliente não encontrado", excecao.getMessage());
    }

    @ExceptionHandler(CpfJaCadastradoException.class)
    ProblemDetail tratarCpfDuplicado(CpfJaCadastradoException excecao) {
        return problema(HttpStatus.CONFLICT, "CPF já cadastrado", excecao.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail tratarConflitoDeDados(DataIntegrityViolationException excecao) {
        return problema(HttpStatus.CONFLICT, "Conflito de dados", "O registro viola uma restrição de unicidade");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail tratarValidacao(MethodArgumentNotValidException excecao) {
        ProblemDetail detalhe = problema(HttpStatus.BAD_REQUEST, "Dados inválidos", "Revise os campos informados");
        Map<String, String> erros = excecao.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage(), (primeiro, ignorado) -> primeiro));
        detalhe.setProperty("errors", erros);
        return detalhe;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail tratarValidacaoDeParametro(ConstraintViolationException excecao) {
        ProblemDetail detalhe = problema(HttpStatus.BAD_REQUEST, "Parâmetro inválido", "Revise os parâmetros informados");
        Map<String, String> erros = excecao.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v.getMessage(),
                        (primeiro, ignorado) -> primeiro));
        detalhe.setProperty("errors", erros);
        return detalhe;
    }

    @ExceptionHandler(IntegracaoScoreException.class)
    ProblemDetail tratarIntegracaoScore(IntegracaoScoreException excecao) {
        HttpStatus status = HttpStatus.valueOf(excecao.getStatusHttp());
        return problema(status, "Falha na consulta de score", excecao.getMessage());
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setType(URI.create("about:blank"));
        return problema;
    }
}
