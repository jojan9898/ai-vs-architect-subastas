package com.subastas.auctionapi.domain;

import com.subastas.auctionapi.domain.exception.OfertaInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubastaTest {

    private static final UUID SUBASTA_ID = UUID.randomUUID();
    private static final UUID USUARIO_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void first_bid_must_be_strictly_positive() {
        Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming");

        assertThatThrownBy(() -> subasta.ofertar(BigDecimal.ZERO, USUARIO_ID, NOW))
                .isInstanceOf(OfertaInsuficienteException.class);
    }

    @Test
    void bid_must_be_strictly_greater_than_current() {
        Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming");
        subasta.ofertar(new BigDecimal("100"), USUARIO_ID, NOW);

        assertThatThrownBy(() -> subasta.ofertar(new BigDecimal("50"), UUID.randomUUID(), NOW))
                .isInstanceOf(OfertaInsuficienteException.class);
    }

    @Test
    void equal_bid_is_rejected() {
        Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming");
        subasta.ofertar(new BigDecimal("100"), USUARIO_ID, NOW);

        assertThatThrownBy(() -> subasta.ofertar(new BigDecimal("100"), UUID.randomUUID(), NOW))
                .isInstanceOf(OfertaInsuficienteException.class);
    }

    @Test
    void successful_bid_updates_highest() {
        Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming");

        subasta.ofertar(new BigDecimal("100"), USUARIO_ID, NOW);

        assertThat(subasta.getOfertaMasAlta()).isPresent();
        assertThat(subasta.getOfertaMasAlta().get().monto()).isEqualByComparingTo("100");
        assertThat(subasta.getMontoMasAlto()).isEqualByComparingTo("100");
    }

    @Test
    void multiple_increasing_bids_succeed() {
        Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming");

        subasta.ofertar(new BigDecimal("10"), USUARIO_ID, NOW);
        subasta.ofertar(new BigDecimal("25"), UUID.randomUUID(), NOW);
        subasta.ofertar(new BigDecimal("30"), UUID.randomUUID(), NOW);

        assertThat(subasta.getMontoMasAlto()).isEqualByComparingTo("30");
    }

    @Test
    void new_auction_highest_starts_at_zero() {
        Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming");

        assertThat(subasta.getOfertaMasAlta()).isEmpty();
        assertThat(subasta.getMontoMasAlto()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void oferta_rejects_non_positive_monto() {
        assertThatThrownBy(() -> new Oferta(BigDecimal.ZERO, USUARIO_ID, NOW))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Oferta(new BigDecimal("-5"), USUARIO_ID, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void oferta_rejects_null_usuario_id() {
        assertThatThrownBy(() -> new Oferta(new BigDecimal("10"), null, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
