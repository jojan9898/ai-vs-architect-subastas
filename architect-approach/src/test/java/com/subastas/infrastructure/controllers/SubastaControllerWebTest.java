package com.subastas.infrastructure.controllers;

import com.subastas.application.BuscarSubastaUseCase;
import com.subastas.application.HacerOfertaUseCase;
import com.subastas.domain.Oferta;
import com.subastas.domain.Subasta;
import com.subastas.domain.exception.OfertaInsuficienteException;
import com.subastas.domain.exception.SubastaNoEncontradaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test del slice web: verifica el contrato HTTP del endpoint de ofertas,
 * especialmente los estados requeridos por la especificacion:
 * 200 (exito), 400 (oferta insuficiente / parametros), 409 (conflicto de
 * concurrencia) y 404 (subasta inexistente).
 *
 * <p>Los casos de uso se mockean para aislar la capa web de la logica de
 * dominio y persistencia, ya cubiertas por sus propios tests.
 */
@WebMvcTest(SubastaController.class)
class SubastaControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HacerOfertaUseCase hacerOfertaUseCase;

    @MockBean
    private BuscarSubastaUseCase buscarSubastaUseCase;

    private static final UUID SUBASTA_ID = UUID.fromString("c0a80001-0000-0000-0000-000000000001");
    private static final UUID USUARIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    @DisplayName("POST oferta valida -> 200 con la subasta actualizada")
    void ofertaValidaRetorna200() throws Exception {
        Oferta oferta = new Oferta(new BigDecimal("150"), USUARIO_ID, Instant.parse("2026-01-01T00:00:00Z"));
        Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming", new BigDecimal("0"), oferta, true, 2L);
        when(hacerOfertaUseCase.ejecutar(eq(SUBASTA_ID), eq(new BigDecimal("150")), eq(USUARIO_ID)))
                .thenReturn(subasta);

        mockMvc.perform(post("/subastas/{id}/ofertar", SUBASTA_ID)
                        .param("monto", "150")
                        .param("usuarioId", USUARIO_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SUBASTA_ID.toString()))
                .andExpect(jsonPath("$.titulo").value("Laptop Gaming"))
                .andExpect(jsonPath("$.ofertaMasAlta.monto").value(150))
                .andExpect(jsonPath("$.ofertaMasAlta.usuarioId").value(USUARIO_ID.toString()))
                .andExpect(jsonPath("$.version").value(2));
    }

    @Test
    @DisplayName("POST oferta no estrictamente mayor -> 400 OFERTA_INSUFICIENTE")
    void ofertaInsuficienteRetorna400() throws Exception {
        when(hacerOfertaUseCase.ejecutar(any(), any(), any()))
                .thenThrow(new OfertaInsuficienteException(new BigDecimal("50"), new BigDecimal("100")));

        mockMvc.perform(post("/subastas/{id}/ofertar", SUBASTA_ID)
                        .param("monto", "50")
                        .param("usuarioId", USUARIO_ID.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("OFERTA_INSUFICIENTE"));
    }

    @Test
    @DisplayName("POST conflicto de concurrencia -> 409 CONFLICTO_VERSION")
    void conflictoConcurrenciaRetorna409() throws Exception {
        when(hacerOfertaUseCase.ejecutar(any(), any(), any()))
                .thenThrow(new OptimisticLockingFailureException("stale"));

        mockMvc.perform(post("/subastas/{id}/ofertar", SUBASTA_ID)
                        .param("monto", "150")
                        .param("usuarioId", USUARIO_ID.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICTO_VERSION"));
    }

    @Test
    @DisplayName("GET subasta inexistente -> 404 SUBASTA_NO_ENCONTRADA")
    void subastaInexistenteRetorna404() throws Exception {
        when(buscarSubastaUseCase.ejecutar(SUBASTA_ID))
                .thenThrow(new SubastaNoEncontradaException(SUBASTA_ID));

        mockMvc.perform(get("/subastas/{id}", SUBASTA_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUBASTA_NO_ENCONTRADA"));
    }

    @Test
    @DisplayName("POST sin monto -> 400")
    void sinMontoRetorna400() throws Exception {
        mockMvc.perform(post("/subastas/{id}/ofertar", SUBASTA_ID)
                        .param("usuarioId", USUARIO_ID.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST con monto mal formado -> 400")
    void montoMalFormadoRetorna400() throws Exception {
        mockMvc.perform(post("/subastas/{id}/ofertar", SUBASTA_ID)
                        .param("monto", "no-es-un-numero")
                        .param("usuarioId", USUARIO_ID.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET subasta existente -> 200")
    void getSubastaExistenteRetorna200() throws Exception {
        Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming", new BigDecimal("0"), null, true, 1L);
        when(buscarSubastaUseCase.ejecutar(SUBASTA_ID)).thenReturn(subasta);

        mockMvc.perform(get("/subastas/{id}", SUBASTA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Laptop Gaming"))
                .andExpect(jsonPath("$.ofertaMasAlta").doesNotExist());
    }
}
