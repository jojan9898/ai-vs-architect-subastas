package com.subastas.auctionapi.domain.exception;

import java.math.BigDecimal;

public class OfertaInsuficienteException extends RuntimeException {

    private final BigDecimal actual;
    private final BigDecimal intentado;

    public OfertaInsuficienteException(BigDecimal actual, BigDecimal intentado) {
        super("La oferta " + intentado + " no es estrictamente mayor que la oferta actual " + actual);
        this.actual = actual;
        this.intentado = intentado;
    }

    public BigDecimal getActual() {
        return actual;
    }

    public BigDecimal getIntentado() {
        return intentado;
    }
}
