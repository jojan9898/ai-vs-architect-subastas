package com.subastas.auctionapi.infrastructure.controllers;

import com.subastas.auctionapi.application.BuscarSubastasUseCase;
import com.subastas.auctionapi.application.HacerOfertaUseCase;
import com.subastas.auctionapi.domain.Subasta;
import com.subastas.auctionapi.domain.exception.OfertaInsuficienteException;
import com.subastas.auctionapi.domain.exception.SubastaNoEncontradaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubastaController.class)
class SubastaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    HacerOfertaUseCase hacerOfertaUseCase;

    @MockBean
    BuscarSubastasUseCase buscarSubastasUseCase;

    @Test
    void get_subastas_returns_200_with_list() throws Exception {
        UUID id = UUID.fromString("c0a80001-0000-0000-0000-000000000001");
        Subasta subasta = Subasta.reconstitute(id, "Laptop Gaming", null);
        when(buscarSubastasUseCase.listar()).thenReturn(List.of(subasta));

        mockMvc.perform(get("/subastas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Laptop Gaming"))
                .andExpect(jsonPath("$[0].ofertaMasAlta").value(0));
    }

    @Test
    void ofertar_success_returns_200_with_updated_auction() throws Exception {
        UUID id = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        Subasta subasta = new Subasta(id, "Laptop Gaming");
        subasta.ofertar(new BigDecimal("100"), usuarioId, Instant.parse("2026-01-01T00:00:00Z"));
        when(hacerOfertaUseCase.ejecutar(eq(id), any(BigDecimal.class), any(UUID.class)))
                .thenReturn(subasta);

        mockMvc.perform(post("/subastas/{id}/ofertar", id)
                        .param("monto", "100")
                        .param("usuarioId", usuarioId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Laptop Gaming"))
                .andExpect(jsonPath("$.ofertaMasAlta").value(100));
    }

    @Test
    void ofertar_insufficient_bid_returns_400() throws Exception {
        UUID id = UUID.randomUUID();
        when(hacerOfertaUseCase.ejecutar(eq(id), any(BigDecimal.class), any(UUID.class)))
                .thenThrow(new OfertaInsuficienteException(BigDecimal.ZERO, BigDecimal.ONE));

        mockMvc.perform(post("/subastas/{id}/ofertar", id)
                        .param("monto", "1")
                        .param("usuarioId", UUID.randomUUID().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("OFERTA_INSUFICIENTE"));
    }

    @Test
    void ofertar_not_found_returns_404() throws Exception {
        UUID id = UUID.randomUUID();
        when(hacerOfertaUseCase.ejecutar(eq(id), any(BigDecimal.class), any(UUID.class)))
                .thenThrow(new SubastaNoEncontradaException(id));

        mockMvc.perform(post("/subastas/{id}/ofertar", id)
                        .param("monto", "10")
                        .param("usuarioId", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUBASTA_NO_ENCONTRADA"));
    }

    @Test
    void ofertar_conflict_returns_409() throws Exception {
        UUID id = UUID.randomUUID();
        when(hacerOfertaUseCase.ejecutar(eq(id), any(BigDecimal.class), any(UUID.class)))
                .thenThrow(new OptimisticLockingFailureException("version conflict"));

        mockMvc.perform(post("/subastas/{id}/ofertar", id)
                        .param("monto", "10")
                        .param("usuarioId", UUID.randomUUID().toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICTO_VERSION"));
    }
}
