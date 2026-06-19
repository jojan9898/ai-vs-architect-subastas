package com.subastas.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto del repositorio de subastas, definido en el dominio.
 *
 * <p>El dominio no conoce JPA ni Spring; solo declara el contrato que la
 * capa de infraestructura debe implementar. De este modo el dominio queda
 * aislado de los detalles de persistencia (inversion de dependencias).
 */
public interface SubastaRepository {

    Optional<Subasta> findById(UUID id);

    Subasta save(Subasta subasta);
}
