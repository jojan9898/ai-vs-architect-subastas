package com.subastas.auctionapi.application;

import com.subastas.auctionapi.domain.Subasta;
import com.subastas.auctionapi.domain.SubastaRepository;
import com.subastas.auctionapi.domain.exception.SubastaNoEncontradaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

@Service
public class HacerOfertaUseCase {

    private final SubastaRepository repository;
    private final Clock clock;

    public HacerOfertaUseCase(SubastaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public Subasta ejecutar(UUID subastaId, BigDecimal monto, UUID usuarioId) {
        Subasta subasta = repository.findById(subastaId)
                .orElseThrow(() -> new SubastaNoEncontradaException(subastaId));
        subasta.ofertar(monto, usuarioId, clock.instant());
        return repository.save(subasta);
    }
}
