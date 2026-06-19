package com.subastas.domain.exception;

/**
 * Se lanza cuando una oferta no supera estrictamente a la oferta mas alta actual
 * de la subasta. Corresponde a una violacion del invariante del Aggregate Root
 * y se traduce a HTTP 400 Bad Request.
 */
public class OfertaInsuficienteException extends RuntimeException {

    private final java.math.BigDecimal montoOfrecido;
    private final java.math.BigDecimal montoMinimo;

    public OfertaInsuficienteException(java.math.BigDecimal montoOfrecido, java.math.BigDecimal montoMinimo) {
        super("La oferta " + montoOfrecido + " debe ser estrictamente mayor que el monto actual " + montoMinimo);
        this.montoOfrecido = montoOfrecido;
        this.montoMinimo = montoMinimo;
    }

    public java.math.BigDecimal getMontoOfrecido() {
        return montoOfrecido;
    }

    public java.math.BigDecimal getMontoMinimo() {
        return montoMinimo;
    }
}
