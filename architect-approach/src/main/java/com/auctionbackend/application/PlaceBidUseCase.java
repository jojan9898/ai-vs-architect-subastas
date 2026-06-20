package com.auctionbackend.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auctionbackend.domain.Auction;
import com.auctionbackend.domain.AuctionRepository;
import com.auctionbackend.domain.exception.AuctionNotFoundException;

/**
 * Use case: place a bid on an auction. It only orchestrates: load the
 * aggregate, let the aggregate enforce the bidding rule, then persist. No
 * business rules live here.
 */
@Service
public class PlaceBidUseCase {

    private final AuctionRepository repository;
    private final Clock clock;

    public PlaceBidUseCase(AuctionRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public Auction execute(UUID auctionId, BigDecimal amount, String userId) {
        Auction auction = repository.findById(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException(auctionId));
        auction.placeBid(amount, userId, clock.instant());
        return repository.save(auction);
    }
}
