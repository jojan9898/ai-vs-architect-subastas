package com.subastas.auctionapi.infrastructure.exceptionhandler;

import com.subastas.auctionapi.domain.exception.OfertaInsuficienteException;
import com.subastas.auctionapi.domain.exception.SubastaNoEncontradaException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(OfertaInsuficienteException.class)
    ResponseEntity<ErrorResponse> handleOfertaInsuficiente(OfertaInsuficienteException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("OFERTA_INSUFICIENTE", ex.getMessage()));
    }

    @ExceptionHandler(SubastaNoEncontradaException.class)
    ResponseEntity<ErrorResponse> handleNoEncontrada(SubastaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("SUBASTA_NO_ENCONTRADA", ex.getMessage()));
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    ResponseEntity<ErrorResponse> handleConflictoVersion(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICTO_VERSION",
                        "La subasta fue modificada por otra transaccion. Reintente."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("ARGUMENTO_INVALIDO", ex.getMessage()));
    }
}
