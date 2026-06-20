package com.subastas.auctionapi.domain;

import com.subastas.auctionapi.domain.exception.OfertaInsuficienteException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Subasta {

    private final UUID id;
    private final String titulo;
    private Oferta ofertaMasAlta;

    public Subasta(UUID id, String titulo) {
        if (id == null) {
            throw new IllegalArgumentException("id es obligatorio");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("titulo es obligatorio");
        }
        this.id = id;
        this.titulo = titulo;
    }

    public static Subasta reconstitute(UUID id, String titulo, Oferta ofertaMasAlta) {
        Subasta subasta = new Subasta(id, titulo);
        subasta.ofertaMasAlta = ofertaMasAlta;
        return subasta;
    }

    public void ofertar(BigDecimal monto, UUID usuarioId, Instant now) {
        Objects.requireNonNull(monto, "monto es obligatorio");
        BigDecimal actual = ofertaMasAlta == null ? BigDecimal.ZERO : ofertaMasAlta.monto();
        if (monto.compareTo(actual) <= 0) {
            throw new OfertaInsuficienteException(actual, monto);
        }
        this.ofertaMasAlta = new Oferta(monto, usuarioId, now);
    }

    public UUID getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public Optional<Oferta> getOfertaMasAlta() {
        return Optional.ofNullable(ofertaMasAlta);
    }

    public BigDecimal getMontoMasAlto() {
        return ofertaMasAlta == null ? BigDecimal.ZERO : ofertaMasAlta.monto();
    }
}
