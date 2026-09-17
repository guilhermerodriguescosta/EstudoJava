package br.com.guilhermecosta.estudojava.exception;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> tratarParametroAusente(MissingServletRequestParameterException exception) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Parâmetro obrigatório ausente", "mensagem", "O parâmetro '" + exception.getParameterName() + "' é obrigatório."));
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> tratarParametroInvalido(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Parâmetro inválido", "mensagem", "O parâmetro '" + exception.getName() + "' possui um valor inválido."));
    }
    @ExceptionHandler(DivisaoPorZeroException.class)
    public ResponseEntity<Map<String, String>> tratarDivisaoPorZero(DivisaoPorZeroException exception) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Operação inválida", "mensagem", exception.getMessage()));
    }
}
