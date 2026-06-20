package com.subastas.auctionapi.infrastructure.seed;

import com.subastas.auctionapi.domain.Subasta;
import com.subastas.auctionapi.domain.SubastaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class SubastaSeedData implements CommandLineRunner {

    static final UUID SEED_ID = UUID.fromString("c0a80001-0000-0000-0000-000000000001");

    private final SubastaRepository repository;

    SubastaSeedData(SubastaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.findById(SEED_ID).isPresent()) {
            return;
        }
        repository.save(new Subasta(SEED_ID, "Laptop Gaming"));
    }
}
