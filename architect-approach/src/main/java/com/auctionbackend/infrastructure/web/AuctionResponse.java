package com.auctionbackend.infrastructure.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.auctionbackend.domain.Auction;
import com.auctionbackend.domain.Bid;

/**
 * API response shape for an auction. The highest bid amount is always present
 * (zero when nobody has bid yet); the user/timestamp are null until the first
 * bid.
 */
public record AuctionResponse(
        UUID id,
        String title,
        BigDecimal highestBid,
        String highestBidUserId,
        Instant highestBidTimestamp) {

    static AuctionResponse from(Auction auction) {
        Bid highest = auction.getHighestBid().orElse(null);
        return new AuctionResponse(
                auction.getId(),
                auction.getTitle(),
                auction.currentHighestAmount(),
                highest == null ? null : highest.userId(),
                highest == null ? null : highest.timestamp());
    }
}
