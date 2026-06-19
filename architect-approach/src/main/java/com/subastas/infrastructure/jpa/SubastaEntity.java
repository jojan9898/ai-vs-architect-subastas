package com.subastas.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA del agregado {@code Subasta}.
 *
 * <p>{@code @Version} implementa optimistic locking: al hacer flush, JPA
 * ejecuta el UPDATE con {@code WHERE id = ? AND version = ?}. Si otro
 * commit gano la carrera y la version en BD cambio, el UPDATE afecta 0 filas
 * y se lanza un conflicto que se traduce a HTTP 409.
 *
 * <p>Esta entidad vive en infraestructura y NO filtra anotaciones al dominio.
 * El adaptador la mapea hacia/desde el agregado puro {@code Subasta}.
 */
@Entity
@Table(name = "subastas")
public class SubastaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "precio_inicial", nullable = false, precision = 19, scale = 2)
    private BigDecimal precioInicial;

    @Embedded
    private OfertaEmbeddable ofertaMasAlta;

    @Column(nullable = false)
    private boolean activa;

    @Version
    @Column(nullable = false)
    private Long version;

    protected SubastaEntity() {
        // requerido por JPA
    }

    public SubastaEntity(UUID id, String titulo, BigDecimal precioInicial,
                         OfertaEmbeddable ofertaMasAlta, boolean activa, Long version) {
        this.id = id;
        this.titulo = titulo;
        this.precioInicial = precioInicial;
        this.ofertaMasAlta = ofertaMasAlta;
        this.activa = activa;
        this.version = version;
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

    public OfertaEmbeddable getOfertaMasAlta() {
        return ofertaMasAlta;
    }

    public void setOfertaMasAlta(OfertaEmbeddable ofertaMasAlta) {
        this.ofertaMasAlta = ofertaMasAlta;
    }

    public boolean isActiva() {
        return activa;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubastaEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
