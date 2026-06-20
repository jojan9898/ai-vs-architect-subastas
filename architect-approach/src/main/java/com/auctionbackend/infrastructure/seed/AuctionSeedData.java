package com.auctionbackend.infrastructure.seed;

import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.auctionbackend.domain.Auction;
import com.auctionbackend.domain.AuctionRepository;

/**
 * Deterministic seed data. The id is fixed so load tests and external scripts
 * can target a known auction without first discovering its id. Idempotent: if
 * the auction already exists the runner does nothing.
 */
@Component
public class AuctionSeedData implements CommandLineRunner {

    public static final UUID SEED_ID = UUID.fromString("c0a80001-0000-0000-0000-000000000001");
    public static final String SEED_TITLE = "Laptop Gaming";

    private final AuctionRepository repository;

    public AuctionSeedData(AuctionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.findById(SEED_ID).isPresent()) {
            return;
        }
        repository.save(new Auction(SEED_ID, SEED_TITLE));
    }
}
