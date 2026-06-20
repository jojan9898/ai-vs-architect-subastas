package com.subastas.auctionapi.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Oferta(BigDecimal monto, UUID usuarioId, Instant timestamp) {

    public Oferta {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("monto debe ser positivo");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId es obligatorio");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp es obligatorio");
        }
    }
}
