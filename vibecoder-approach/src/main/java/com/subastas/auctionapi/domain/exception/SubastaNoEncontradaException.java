package com.subastas.auctionapi.domain.exception;

import java.util.UUID;

public class SubastaNoEncontradaException extends RuntimeException {

    public SubastaNoEncontradaException(UUID id) {
        super("Subasta no encontrada: " + id);
    }
}
