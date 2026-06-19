package com.subastas.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Representacion persistente del value object {@code Oferta}.
 *
 * <p>Se modela como {@code @Embeddable} porque la oferta pertenece al agregado
 * Subasta y no tiene identidad propia: su ciclo de vida esta ligado al agregado.
 */
@Embeddable
public class OfertaEmbeddable {

    @Column(name = "oferta_monto", precision = 19, scale = 2)
    private BigDecimal monto;

    @Column(name = "oferta_usuario_id")
    private UUID usuarioId;

    @Column(name = "oferta_timestamp")
    private Instant timestamp;

    protected OfertaEmbeddable() {
        // requerido por JPA
    }

    public OfertaEmbeddable(BigDecimal monto, UUID usuarioId, Instant timestamp) {
        this.monto = monto;
        this.usuarioId = usuarioId;
        this.timestamp = timestamp;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OfertaEmbeddable that)) return false;
        return Objects.equals(monto, that.monto)
                && Objects.equals(usuarioId, that.usuarioId)
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monto, usuarioId, timestamp);
    }
}
