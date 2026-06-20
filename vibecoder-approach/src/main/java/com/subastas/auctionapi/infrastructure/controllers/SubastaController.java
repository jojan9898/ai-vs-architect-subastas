package com.subastas.auctionapi.infrastructure.controllers;

import com.subastas.auctionapi.application.BuscarSubastasUseCase;
import com.subastas.auctionapi.application.HacerOfertaUseCase;
import com.subastas.auctionapi.domain.Subasta;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/subastas")
class SubastaController {

    private final HacerOfertaUseCase hacerOfertaUseCase;
    private final BuscarSubastasUseCase buscarSubastasUseCase;

    SubastaController(HacerOfertaUseCase hacerOfertaUseCase, BuscarSubastasUseCase buscarSubastasUseCase) {
        this.hacerOfertaUseCase = hacerOfertaUseCase;
        this.buscarSubastasUseCase = buscarSubastasUseCase;
    }

    @GetMapping
    List<SubastaResponse> listar() {
        return buscarSubastasUseCase.listar().stream()
                .map(SubastaResponse::from)
                .toList();
    }

    @PostMapping("/{id}/ofertar")
    ResponseEntity<SubastaResponse> ofertar(
            @PathVariable UUID id,
            @RequestParam BigDecimal monto,
            @RequestParam UUID usuarioId) {
        Subasta subasta = hacerOfertaUseCase.ejecutar(id, monto, usuarioId);
        return ResponseEntity.ok(SubastaResponse.from(subasta));
    }
}
