package com.subastas.auctionapi.infrastructure.jpa;

import com.subastas.auctionapi.domain.Subasta;
import com.subastas.auctionapi.domain.SubastaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class SubastaRepositoryAdapter implements SubastaRepository {

    private final SubastaJpaRepository jpaRepository;

    SubastaRepositoryAdapter(SubastaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Subasta> findById(UUID id) {
        return jpaRepository.findById(id).map(SubastaEntity::toDomain);
    }

    @Override
    public List<Subasta> findAll() {
        return jpaRepository.findAll().stream()
                .map(SubastaEntity::toDomain)
                .toList();
    }

    @Override
    public Subasta save(Subasta subasta) {
        SubastaEntity entity = jpaRepository.findById(subasta.getId())
                .orElseGet(() -> SubastaEntity.fromDomain(subasta));
        entity.applyFromDomain(subasta);
        return jpaRepository.save(entity).toDomain();
    }
}
