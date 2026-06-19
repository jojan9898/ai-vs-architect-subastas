package com.subastas.application;

import com.subastas.domain.Oferta;
import com.subastas.domain.Subasta;
import com.subastas.domain.SubastaRepository;
import com.subastas.domain.exception.SubastaNoEncontradaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

/**
 * Caso de uso: realizar una oferta sobre una subasta.
 *
 * <p>Orquesta la carga del agregado, la aplicacion del invariante dentro del
 * propio agregado y su persistencia. Define el limite de transaccion: toda la
 * operacion (lectura + modificacion + guardado) ocurre en una misma
 * transaccion, de modo que el {@code @Version} de JPA detecta conflictos de
 * concurrencia al hacer flush.
 *
 * <p>El caso de uso no conoce JPA; depende del puerto {@link SubastaRepository}.
 * El conflicto optimista se materializa en la infraestructura y se traduce a
 * HTTP 409 en el exception handler.
 */
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

        Oferta oferta = new Oferta(monto, usuarioId, java.time.Instant.now(clock));
        subasta.hacerOferta(oferta);

        return repository.save(subasta);
    }
}
