package com.subastas.domain;

import com.subastas.domain.exception.OfertaInsuficienteException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubastaTest {

    private static final UUID SUBASTA_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Invariante de oferta estrictamente mayor")
    class InvarianteOferta {

        @Test
        @DisplayName("la primera oferta debe ser estrictamente mayor que el precio inicial")
        void primeraOfertaDebeSerMayorQuePrecioInicial() {
            Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming", new BigDecimal("0"));

            subasta.hacerOferta(oferta(new BigDecimal("1")));

            assertThat(subasta.getOfertaMasAlta().monto()).isEqualByComparingTo("1");
        }

        @Test
        @DisplayName("una oferta igual al precio inicial se rechaza (no es estrictamente mayor)")
        void ofertaIgualAlPrecioInicialSeRechaza() {
            Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming", new BigDecimal("0"));

            assertThatThrownBy(() -> subasta.hacerOferta(oferta(new BigDecimal("0"))))
                    .isInstanceOf(OfertaInsuficienteException.class);
        }

        @Test
        @DisplayName("una oferta menor que la oferta actual se rechaza")
        void ofertaMenorSeRechaza() {
            Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming", new BigDecimal("0"));
            subasta.hacerOferta(oferta(new BigDecimal("100")));

            assertThatThrownBy(() -> subasta.hacerOferta(oferta(new BigDecimal("50"))))
                    .isInstanceOf(OfertaInsuficienteException.class);
        }

        @Test
        @DisplayName("una oferta igual a la oferta actual se rechaza (debe ser estrictamente mayor)")
        void ofertaIgualSeRechaza() {
            Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming", new BigDecimal("0"));
            subasta.hacerOferta(oferta(new BigDecimal("100")));

            assertThatThrownBy(() -> subasta.hacerOferta(oferta(new BigDecimal("100"))))
                    .isInstanceOf(OfertaInsuficienteException.class);
        }

        @Test
        @DisplayName("una oferta estrictamente mayor se acepta y reemplaza la anterior")
        void ofertaMayorSeAcepta() {
            Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming", new BigDecimal("0"));
            subasta.hacerOferta(oferta(new BigDecimal("100")));
            subasta.hacerOferta(oferta(new BigDecimal("150.50")));

            assertThat(subasta.getOfertaMasAlta().monto()).isEqualByComparingTo("150.50");
        }

        @Test
        @DisplayName("la excepcion informa el monto ofrecido y el minimo esperado")
        void excepcionInformaMontos() {
            Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming", new BigDecimal("0"));
            subasta.hacerOferta(oferta(new BigDecimal("100")));

            assertThatThrownBy(() -> subasta.hacerOferta(oferta(new BigDecimal("100"))))
                    .isInstanceOfSatisfying(OfertaInsuficienteException.class, ex -> {
                        assertThat(ex.getMontoOfrecido()).isEqualByComparingTo("100");
                        assertThat(ex.getMontoMinimo()).isEqualByComparingTo("100");
                    });
        }
    }

    @Nested
    @DisplayName("Construccion del agregado")
    class Construccion {

        @Test
        @DisplayName("una subasta nueva no tiene ofertas y esta activa")
        void nuevaSinOfertas() {
            Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming", new BigDecimal("0"));

            assertThat(subasta.getOfertaMasAlta()).isNull();
            assertThat(subasta.isActiva()).isTrue();
            assertThat(subasta.getVersion()).isNull();
        }

        @Test
        @DisplayName("titulo vacio lanza excepcion")
        void tituloVacioLanza() {
            assertThatThrownBy(() -> new Subasta(SUBASTA_ID, "  ", new BigDecimal("0")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("precio inicial negativo lanza excepcion")
        void precioNegativoLanza() {
            assertThatThrownBy(() -> new Subasta(SUBASTA_ID, "Laptop", new BigDecimal("-1")))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private Oferta oferta(BigDecimal monto) {
        return new Oferta(monto, UUID.randomUUID(), Instant.now());
    }
}
