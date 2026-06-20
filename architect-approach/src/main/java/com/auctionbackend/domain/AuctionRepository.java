package com.auctionbackend.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port declared in the domain layer. The domain depends on this
 * abstraction, never on JPA. The adapter lives in infrastructure.
 */
public interface AuctionRepository {

    Optional<Auction> findById(UUID id);

    List<Auction> findAll();

    Auction save(Auction auction);
}
