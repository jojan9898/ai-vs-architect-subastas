package com.subastas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfertaTest {

    @Test
    @DisplayName("una oferta valida se construye correctamente")
    void ofertaValida() {
        UUID usuario = UUID.randomUUID();
        Instant ts = Instant.now();

        Oferta oferta = new Oferta(new BigDecimal("250"), usuario, ts);

        assertThat(oferta.monto()).isEqualByComparingTo("250");
        assertThat(oferta.usuarioId()).isEqualTo(usuario);
        assertThat(oferta.timestamp()).isEqualTo(ts);
    }

    @Test
    @DisplayName("monto null lanza NPE")
    void montoNullLanza() {
        assertThatThrownBy(() -> new Oferta(null, UUID.randomUUID(), Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("usuarioId null lanza NPE")
    void usuarioNullLanza() {
        assertThatThrownBy(() -> new Oferta(new BigDecimal("1"), null, Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("timestamp null lanza NPE")
    void timestampNullLanza() {
        assertThatThrownBy(() -> new Oferta(new BigDecimal("1"), UUID.randomUUID(), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("monto negativo lanza IllegalArgumentException")
    void montoNegativoLanza() {
        assertThatThrownBy(() -> new Oferta(new BigDecimal("-5"), UUID.randomUUID(), Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
