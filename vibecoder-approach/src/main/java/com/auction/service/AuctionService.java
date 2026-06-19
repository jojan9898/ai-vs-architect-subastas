package com.auction.service;

import com.auction.domain.Auction;
import com.auction.exception.AuctionNotFoundException;
import com.auction.exception.BidNotHigherException;
import com.auction.repository.AuctionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AuctionService {

    private final AuctionRepository auctionRepository;

    public AuctionService(AuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    @Transactional
    public Auction placeBid(Long auctionId, BigDecimal amount, String userId) {
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new AuctionNotFoundException(auctionId));

        if (amount.compareTo(auction.getCurrentHighestBid()) <= 0) {
            throw new BidNotHigherException(auction.getCurrentHighestBid());
        }

        auction.placeBid(amount, userId);
        return auctionRepository.save(auction);
    }
}
