package com.auctionbackend.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auctionbackend.domain.Auction;
import com.auctionbackend.domain.AuctionRepository;

/**
 * Use case: list all auctions. Read-only use case over the repository port.
 */
@Service
public class ListAuctionsUseCase {

    private final AuctionRepository repository;

    public ListAuctionsUseCase(AuctionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Auction> execute() {
        return repository.findAll();
    }
}
