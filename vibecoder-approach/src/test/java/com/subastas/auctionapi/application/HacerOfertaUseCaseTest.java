package com.subastas.auctionapi.application;

import com.subastas.auctionapi.domain.Subasta;
import com.subastas.auctionapi.domain.SubastaRepository;
import com.subastas.auctionapi.domain.exception.OfertaInsuficienteException;
import com.subastas.auctionapi.domain.exception.SubastaNoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HacerOfertaUseCaseTest {

    private static final UUID SUBASTA_ID = UUID.fromString("c0a80001-0000-0000-0000-000000000001");
    private static final UUID USUARIO_ID = UUID.fromString("c0a80002-0000-0000-0000-000000000002");
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    SubastaRepository repository;

    @Mock
    Clock clock;

    @InjectMocks
    HacerOfertaUseCase useCase;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(NOW);
    }

    @Test
    void successful_bid_saves_updated_subasta() {
        Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming");
        when(repository.findById(SUBASTA_ID)).thenReturn(Optional.of(subasta));
        when(repository.save(any(Subasta.class))).thenAnswer(inv -> inv.getArgument(0));

        Subasta result = useCase.ejecutar(SUBASTA_ID, new BigDecimal("100"), USUARIO_ID);

        assertThat(result.getOfertaMasAlta()).isPresent();
        assertThat(result.getOfertaMasAlta().get().monto()).isEqualByComparingTo("100");
        verify(repository).save(any(Subasta.class));
    }

    @Test
    void rejects_bid_not_higher_than_current() {
        Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming");
        subasta.ofertar(new BigDecimal("100"), USUARIO_ID, NOW);
        when(repository.findById(SUBASTA_ID)).thenReturn(Optional.of(subasta));

        assertThatThrownBy(() -> useCase.ejecutar(SUBASTA_ID, new BigDecimal("50"), USUARIO_ID))
                .isInstanceOf(OfertaInsuficienteException.class);
        verify(repository, never()).save(any(Subasta.class));
    }

    @Test
    void rejects_zero_bid_on_new_auction() {
        Subasta subasta = new Subasta(SUBASTA_ID, "Laptop Gaming");
        when(repository.findById(SUBASTA_ID)).thenReturn(Optional.of(subasta));

        assertThatThrownBy(() -> useCase.ejecutar(SUBASTA_ID, BigDecimal.ZERO, USUARIO_ID))
                .isInstanceOf(OfertaInsuficienteException.class);
    }

    @Test
    void throws_when_subasta_not_found() {
        when(repository.findById(SUBASTA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(SUBASTA_ID, new BigDecimal("100"), USUARIO_ID))
                .isInstanceOf(SubastaNoEncontradaException.class);
    }
}
