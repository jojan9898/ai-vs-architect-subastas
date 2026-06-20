package com.subastas.auctionapi.infrastructure.jpa;

import com.subastas.auctionapi.domain.Oferta;
import com.subastas.auctionapi.domain.Subasta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "subastas")
class SubastaEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false)
    private String titulo;

    private BigDecimal montoMasAlto;
    private UUID usuarioIdUltimaOferta;
    private Instant timestampUltimaOferta;

    protected SubastaEntity() {
    }

    static SubastaEntity fromDomain(Subasta subasta) {
        SubastaEntity entity = new SubastaEntity();
        entity.id = subasta.getId();
        entity.titulo = subasta.getTitulo();
        entity.applyOferta(subasta);
        return entity;
    }

    void applyFromDomain(Subasta subasta) {
        this.titulo = subasta.getTitulo();
        applyOferta(subasta);
    }

    private void applyOferta(Subasta subasta) {
        Optional<Oferta> oferta = subasta.getOfertaMasAlta();
        if (oferta.isPresent()) {
            this.montoMasAlto = oferta.get().monto();
            this.usuarioIdUltimaOferta = oferta.get().usuarioId();
            this.timestampUltimaOferta = oferta.get().timestamp();
        } else {
            this.montoMasAlto = null;
            this.usuarioIdUltimaOferta = null;
            this.timestampUltimaOferta = null;
        }
    }

    Subasta toDomain() {
        Oferta oferta = null;
        if (montoMasAlto != null) {
            oferta = new Oferta(montoMasAlto, usuarioIdUltimaOferta, timestampUltimaOferta);
        }
        return Subasta.reconstitute(id, titulo, oferta);
    }
}
