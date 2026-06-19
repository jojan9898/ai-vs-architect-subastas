package com.subastas.infrastructure.controllers;

import com.subastas.application.BuscarSubastaUseCase;
import com.subastas.application.HacerOfertaUseCase;
import com.subastas.domain.Subasta;
import com.subastas.infrastructure.controllers.dto.SubastaResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Controlador REST del recurso subastas.
 *
 * <p>POST /subastas/{id}/ofertar?monto=X&usuarioId=Y realiza una oferta.
 * GET  /subastas/{id}                  devuelve el estado actual.
 *
 * <p>El controlador es fino: solo orquesta HTTP y delega la logica en los
 * casos de uso. No contiene reglas de negocio.
 */
@RestController
@RequestMapping("/subastas")
public class SubastaController {

    private final HacerOfertaUseCase hacerOfertaUseCase;
    private final BuscarSubastaUseCase buscarSubastaUseCase;

    public SubastaController(HacerOfertaUseCase hacerOfertaUseCase,
                             BuscarSubastaUseCase buscarSubastaUseCase) {
        this.hacerOfertaUseCase = hacerOfertaUseCase;
        this.buscarSubastaUseCase = buscarSubastaUseCase;
    }

    @PostMapping("/{id}/ofertar")
    public ResponseEntity<SubastaResponse> ofertar(
            @PathVariable UUID id,
            @RequestParam BigDecimal monto,
            @RequestParam UUID usuarioId) {

        Subasta subasta = hacerOfertaUseCase.ejecutar(id, monto, usuarioId);
        return ResponseEntity.ok(SubastaResponse.from(subasta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubastaResponse> obtener(@PathVariable UUID id) {
        Subasta subasta = buscarSubastaUseCase.ejecutar(id);
        return ResponseEntity.ok(SubastaResponse.from(subasta));
    }
}
