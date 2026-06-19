package com.subastas.infrastructure.jpa;

import com.subastas.domain.Oferta;
import com.subastas.domain.Subasta;
import com.subastas.domain.SubastaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador que implementa el puerto del dominio {@link SubastaRepository}
 * usando JPA.
 *
 * <p>Es el unico punto que conoce tanto el modelo de persistencia (entidades
 * JPA) como el modelo del dominio. Su responsabilidad es mapear entre ambos
 * sin filtrar anotaciones de persistencia al dominio.
 *
 * <p>El {@code save} construye una entidad detached conservando la version que
 * traia el agregado desde la carga. Al delegar en {@code saveAndFlush} (que hace
 * {@code merge} + flush inmediato), JPA ejecuta el UPDATE con la clausula de
 * version ahi mismo: si la version en BD cambio por otra transaccion, el UPDATE
 * afecta 0 filas y se lanza el conflicto de optimistic locking en el limite del
 * repositorio (donde Spring lo traduce a
 * {@code ObjectOptimisticLockingFailureException} -> HTTP 409).
 *
 * <p>Ademas, al hacer flush antes de devolver, la version del agregado
 * resultante refleja ya el incremento, de modo que la respuesta HTTP incluye
 * la version correcta.
 */
@Repository
public class SubastaRepositoryAdapter implements SubastaRepository {

    private final SubastaJpaRepository jpaRepository;

    public SubastaRepositoryAdapter(SubastaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subasta> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional
    public Subasta save(Subasta subasta) {
        SubastaEntity entity = toEntity(subasta);
        SubastaEntity saved = jpaRepository.saveAndFlush(entity);
        return toDomain(saved);
    }

    private Subasta toDomain(SubastaEntity entity) {
        Oferta oferta = entity.getOfertaMasAlta() == null ? null : new Oferta(
                entity.getOfertaMasAlta().getMonto(),
                entity.getOfertaMasAlta().getUsuarioId(),
                entity.getOfertaMasAlta().getTimestamp()
        );
        return new Subasta(
                entity.getId(),
                entity.getTitulo(),
                entity.getPrecioInicial(),
                oferta,
                entity.isActiva(),
                entity.getVersion()
        );
    }

    private SubastaEntity toEntity(Subasta subasta) {
        OfertaEmbeddable embeddable = subasta.getOfertaMasAlta() == null ? null : new OfertaEmbeddable(
                subasta.getOfertaMasAlta().monto(),
                subasta.getOfertaMasAlta().usuarioId(),
                subasta.getOfertaMasAlta().timestamp()
        );
        return new SubastaEntity(
                subasta.getId(),
                subasta.getTitulo(),
                subasta.getPrecioInicial(),
                embeddable,
                subasta.isActiva(),
                subasta.getVersion()
        );
    }
}
