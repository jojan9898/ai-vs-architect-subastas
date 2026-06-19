package com.subastas.infrastructure.controllers.dto;

import com.subastas.domain.Oferta;
import com.subastas.domain.Subasta;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO de salida para una subasta.
 *
 * <p>El dominio nunca se expone directamente a la API: se mapea a este DTO
 * para desacoplar el contrato HTTP del modelo de dominio.
 */
public record SubastaResponse(
        UUID id,
        String titulo,
        BigDecimal precioInicial,
        OfertaResponse ofertaMasAlta,
        boolean activa,
        Long version
) {

    public static SubastaResponse from(Subasta subasta) {
        OfertaResponse oferta = subasta.getOfertaMasAlta() == null
                ? null
                : OfertaResponse.from(subasta.getOfertaMasAlta());
        return new SubastaResponse(
                subasta.getId(),
                subasta.getTitulo(),
                subasta.getPrecioInicial(),
                oferta,
                subasta.isActiva(),
                subasta.getVersion()
        );
    }

    public record OfertaResponse(BigDecimal monto, UUID usuarioId, Instant timestamp) {
        public static OfertaResponse from(Oferta oferta) {
            return new OfertaResponse(oferta.monto(), oferta.usuarioId(), oferta.timestamp());
        }
    }
}
