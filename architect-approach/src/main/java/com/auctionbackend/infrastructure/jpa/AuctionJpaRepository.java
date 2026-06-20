package com.auctionbackend.infrastructure.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository. The domain-facing port is
 * {@link com.auctionbackend.domain.AuctionRepository}; this is the raw JPA
 * interface used by the adapter.
 */
@Repository
interface AuctionJpaRepository extends JpaRepository<AuctionEntity, UUID> {
}
