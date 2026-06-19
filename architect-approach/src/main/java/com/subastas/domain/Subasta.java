package com.subastas.domain;

import com.subastas.domain.exception.OfertaInsuficienteException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del contexto de subastas.
 *
 * <p>Es el unico responsable de mantener su invariante de consistencia:
 * una nueva oferta debe ser estrictamente mayor que la oferta mas alta actual
 * (o que el precio inicial si aun no hay ofertas). Ningun codigo externo puede
 * mutar su estado directamente; toda modificacion pasa por {@link #hacerOferta}.
 *
 * <p>Mantiene una version ({@code version}) que el adaptador de persistencia
 * mapea a {@code @Version} de JPA para implementar optimistic locking. La version
 * forma parte de la frontera del agregado, de modo que el conflicto de
 * concurrencia se detecta en el limite del agregado, no fuera de el.
 */
public class Subasta {

    private final UUID id;
    private final String titulo;
    private final BigDecimal precioInicial;
    private Oferta ofertaMasAlta;
    private final boolean activa;
    private final Long version;

    /**
     * Crea una subasta nueva (sin ofertas, sin version asignada).
     */
    public Subasta(UUID id, String titulo, BigDecimal precioInicial) {
        this(id, titulo, precioInicial, null, true, null);
    }

    /**
     * Reconstituye una subasta desde la persistencia, conservando su version
     * para que el optimistic locking funcione al guardarla de nuevo.
     */
    public Subasta(UUID id,
                   String titulo,
                   BigDecimal precioInicial,
                   Oferta ofertaMasAlta,
                   boolean activa,
                   Long version) {
        this.id = Objects.requireNonNull(id, "El id no puede ser null");
        this.titulo = requireNonBlank(titulo, "El titulo no puede estar vacio");
        this.precioInicial = Objects.requireNonNull(precioInicial, "El precio inicial no puede ser null");
        if (precioInicial.signum() < 0) {
            throw new IllegalArgumentException("El precio inicial no puede ser negativo");
        }
        this.ofertaMasAlta = ofertaMasAlta;
        this.activa = activa;
        this.version = version;
    }

    /**
     * Aplica una nueva oferta al agregado, validando el invariante.
     *
     * <p>El invariante: la oferta debe ser estrictamente mayor que el monto
     * actual (la oferta mas alta existente, o el precio inicial si no hay
     * ofertas). Si no lo es, se lanza {@link OfertaInsuficienteException}.
     *
     * @param nuevaOferta la oferta a aplicar; debe estar ya construida y validada
     * @throws OfertaInsuficienteException si la oferta no supera estrictamente
     *         el monto actual
     */
    public void hacerOferta(Oferta nuevaOferta) {
        Objects.requireNonNull(nuevaOferta, "La oferta no puede ser null");
        BigDecimal montoActual = ofertaMasAlta != null ? ofertaMasAlta.monto() : precioInicial;
        if (nuevaOferta.monto().compareTo(montoActual) <= 0) {
            throw new OfertaInsuficienteException(nuevaOferta.monto(), montoActual);
        }
        this.ofertaMasAlta = nuevaOferta;
    }

    public UUID getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public BigDecimal getPrecioInicial() {
        return precioInicial;
    }

    public Oferta getOfertaMasAlta() {
        return ofertaMasAlta;
    }

    public boolean isActiva() {
        return activa;
    }

    public Long getVersion() {
        return version;
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
