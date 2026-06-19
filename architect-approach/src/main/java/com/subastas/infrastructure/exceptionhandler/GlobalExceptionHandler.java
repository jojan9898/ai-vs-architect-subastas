package com.subastas.infrastructure.exceptionhandler;

import com.subastas.domain.exception.OfertaInsuficienteException;
import com.subastas.domain.exception.SubastaNoEncontradaException;
import com.subastas.infrastructure.controllers.dto.ErrorResponse;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Traduce excepciones del dominio y de infraestructura a respuestas HTTP.
 *
 * <p>Tabla de mapeo:
 * <ul>
 *   <li>{@link OfertaInsuficienteException} -> 400 (invariante violado)</li>
 *   <li>parametro ausente / mal tipado            -> 400</li>
 *   <li>{@link SubastaNoEncontradaException} -> 404</li>
 *   <li>conflicto de optimistic locking          -> 409</li>
 *   <li>resto                                       -> 500</li>
 * </ul>
 *
 * <p>El conflicto de concurrencia (409) se detecta por {@code @Version} de JPA
 * al hacer flush del UPDATE. Tanto la excepcion de Spring
 * {@link OptimisticLockingFailureException} como la de JPA
 * {@link OptimisticLockException} se tratan como 409, para cubrir el caso en
 * que la traduccion de excepciones de persistencia no haya ocurrido.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OfertaInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleOfertaInsuficiente(OfertaInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("OFERTA_INSUFICIENTE", ex.getMessage()));
    }

    @ExceptionHandler(SubastaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleNoEncontrada(SubastaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("SUBASTA_NO_ENCONTRADA", ex.getMessage()));
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ErrorResponse> handleConflictoConcurrencia(Exception ex) {
        log.warn("Conflicto de optimistic locking: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "CONFLICTO_VERSION",
                        "Conflicto de concurrencia: la subasta fue modificada por otra transaccion. Reintente."));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleParametroInvalido(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("PARAMETRO_INVALIDO", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenerico(Exception ex) {
        log.error("Error inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERROR_INTERNO", "Ocurrio un error inesperado"));
    }
}
