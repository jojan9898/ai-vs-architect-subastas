package com.auctionbackend.infrastructure.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.auctionbackend.domain.Auction;
import com.auctionbackend.domain.AuctionRepository;
import com.auctionbackend.domain.Bid;
import com.auctionbackend.domain.exception.AuctionNotFoundException;

/**
 * Adapter implementing the domain repository port. Maps between the pure
 * domain aggregate and the JPA entity.
 *
 * <p>Concurrency: {@code save} loads the managed entity within the same
 * transaction and mutates it in place. On flush, Hibernate issues an
 * {@code UPDATE ... WHERE id = ? AND version = ?}. If a concurrent transaction
 * already bumped the version, zero rows are updated and Hibernate throws an
 * {@code OptimisticLockingFailureException}, which the web layer maps to
 * HTTP 409. The conflict is visible, never silent.
 */
@Repository
class AuctionRepositoryAdapter implements AuctionRepository {

    private final AuctionJpaRepository jpaRepository;

    AuctionRepositoryAdapter(AuctionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Auction> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Auction> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional
    public Auction save(Auction auction) {
        AuctionEntity entity = jpaRepository.findById(auction.getId())
                .map(existing -> {
                    applyBid(existing, auction);
                    return existing;
                })
                .orElseGet(() -> toNewEntity(auction));
        // saveAndFlush forces the version-checked UPDATE to run inside this
        // method so the optimistic-lock failure surfaces here, not at commit.
        AuctionEntity saved = jpaRepository.saveAndFlush(entity);
        return toDomain(saved);
    }

    private Auction toDomain(AuctionEntity entity) {
        Bid highestBid = null;
        if (entity.getHighestBidAmount() != null) {
            highestBid = new Bid(
                    entity.getHighestBidAmount(),
                    entity.getHighestBidUserId(),
                    entity.getHighestBidTimestamp());
        }
        return Auction.reconstitute(entity.getId(), entity.getTitle(), highestBid);
    }

    private AuctionEntity toNewEntity(Auction auction) {
        AuctionEntity entity = new AuctionEntity();
        entity.setId(auction.getId());
        entity.setTitle(auction.getTitle());
        applyBid(entity, auction);
        return entity;
    }

    private void applyBid(AuctionEntity entity, Auction auction) {
        entity.setTitle(auction.getTitle());
        if (auction.getHighestBid().isPresent()) {
            Bid bid = auction.getHighestBid().get();
            entity.setHighestBidAmount(bid.amount());
            entity.setHighestBidUserId(bid.userId());
            entity.setHighestBidTimestamp(bid.timestamp());
        } else {
            entity.setHighestBidAmount(null);
            entity.setHighestBidUserId(null);
            entity.setHighestBidTimestamp(null);
        }
    }
}
