package com.subastas.auctionapi.infrastructure.controllers;

import com.subastas.auctionapi.domain.Oferta;
import com.subastas.auctionapi.domain.Subasta;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SubastaResponse(
        UUID id,
        String titulo,
        BigDecimal ofertaMasAlta,
        UUID usuarioIdUltimaOferta,
        Instant timestampUltimaOferta) {

    static SubastaResponse from(Subasta subasta) {
        Oferta oferta = subasta.getOfertaMasAlta().orElse(null);
        return new SubastaResponse(
                subasta.getId(),
                subasta.getTitulo(),
                oferta == null ? BigDecimal.ZERO : oferta.monto(),
                oferta == null ? null : oferta.usuarioId(),
                oferta == null ? null : oferta.timestamp());
    }
}
