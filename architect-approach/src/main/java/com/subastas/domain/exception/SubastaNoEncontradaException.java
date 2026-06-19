package com.subastas.domain.exception;

import java.util.UUID;

/**
 * Se lanza cuando no se encuentra una subasta por su identificador.
 * Se traduce a HTTP 404 Not Found.
 */
public class SubastaNoEncontradaException extends RuntimeException {

    private final UUID subastaId;

    public SubastaNoEncontradaException(UUID subastaId) {
        super("No existe la subasta con id " + subastaId);
        this.subastaId = subastaId;
    }

    public UUID getSubastaId() {
        return subastaId;
    }
}
