package com.subastas.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositorio Spring Data JPA para {@link SubastaEntity}.
 */
public interface SubastaJpaRepository extends JpaRepository<SubastaEntity, UUID> {
}
