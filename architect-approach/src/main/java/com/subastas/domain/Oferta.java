package com.subastas.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object inmutable que representa una oferta sobre una subasta.
 *
 * <p>Es completamente inmutable: una vez creado no puede modificarse.
 * Su validez se garantiza en el constructor compacto del record, de modo que
 * nunca puede existir una instancia invalida de {@code Oferta}.
 */
public record Oferta(BigDecimal monto, UUID usuarioId, Instant timestamp) {

    public Oferta {
        Objects.requireNonNull(monto, "El monto no puede ser null");
        Objects.requireNonNull(usuarioId, "El usuarioId no puede ser null");
        Objects.requireNonNull(timestamp, "El timestamp no puede ser null");
        if (monto.signum() < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
    }
}
