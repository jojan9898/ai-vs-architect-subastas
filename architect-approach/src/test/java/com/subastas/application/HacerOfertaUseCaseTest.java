package com.subastas.application;

import com.subastas.domain.Oferta;
import com.subastas.domain.Subasta;
import com.subastas.domain.SubastaRepository;
import com.subastas.domain.exception.OfertaInsuficienteException;
import com.subastas.domain.exception.SubastaNoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HacerOfertaUseCaseTest {

    private SubastaRepository repository;
    private Clock fixedClock;
    private HacerOfertaUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(SubastaRepository.class);
        fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        useCase = new HacerOfertaUseCase(repository, fixedClock);
    }

    @Test
    @DisplayName("aplica la oferta y persiste cuando el monto es estrictamente mayor")
    void aplicaOfertaValida() {
        UUID subastaId = UUID.randomUUID();
        Subasta subasta = new Subasta(subastaId, "Laptop Gaming", new BigDecimal("0"));
        when(repository.findById(subastaId)).thenReturn(Optional.of(subasta));
        when(repository.save(any(Subasta.class))).thenAnswer(inv -> inv.getArgument(0));

        Subasta resultado = useCase.ejecutar(subastaId, new BigDecimal("100"), UUID.randomUUID());

        assertThat(resultado.getOfertaMasAlta()).isNotNull();
        assertThat(resultado.getOfertaMasAlta().monto()).isEqualByComparingTo("100");
        assertThat(resultado.getOfertaMasAlta().timestamp())
                .isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        verify(repository, times(1)).save(any(Subasta.class));
    }

    @Test
    @DisplayName("lanza SubastaNoEncontradaException si no existe la subasta")
    void lanzaSiNoExiste() {
        UUID subastaId = UUID.randomUUID();
        when(repository.findById(subastaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(subastaId, new BigDecimal("1"), UUID.randomUUID()))
                .isInstanceOf(SubastaNoEncontradaException.class);
        verify(repository, never()).save(any(Subasta.class));
    }

    @Test
    @DisplayName("propaga OfertaInsuficienteException del agregado y no persiste")
    void propagaInvariante() {
        UUID subastaId = UUID.randomUUID();
        Subasta subasta = new Subasta(subastaId, "Laptop Gaming", new BigDecimal("100"));
        when(repository.findById(subastaId)).thenReturn(Optional.of(subasta));

        assertThatThrownBy(() -> useCase.ejecutar(subastaId, new BigDecimal("50"), UUID.randomUUID()))
                .isInstanceOf(OfertaInsuficienteException.class);
        verify(repository, never()).save(any(Subasta.class));
    }

    @Test
    @DisplayName("la oferta usa el timestamp del Clock inyectado")
    void usaClockInyectado() {
        UUID subastaId = UUID.randomUUID();
        Subasta subasta = new Subasta(subastaId, "Laptop Gaming", new BigDecimal("0"));
        when(repository.findById(subastaId)).thenReturn(Optional.of(subasta));
        when(repository.save(any(Subasta.class))).thenAnswer(inv -> inv.getArgument(0));

        Subasta resultado = useCase.ejecutar(subastaId, new BigDecimal("1"), UUID.randomUUID());
        Oferta oferta = resultado.getOfertaMasAlta();

        assertThat(oferta.timestamp()).isEqualTo(Instant.now(fixedClock));
    }
}
