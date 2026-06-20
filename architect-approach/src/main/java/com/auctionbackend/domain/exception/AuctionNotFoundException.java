package com.auctionbackend.domain.exception;

import java.util.UUID;

/**
 * Domain exception: the requested auction does not exist.
 * Maps to HTTP 404.
 */
public final class AuctionNotFoundException extends RuntimeException {

    private final UUID auctionId;

    public AuctionNotFoundException(UUID auctionId) {
        super("Auction " + auctionId + " was not found");
        this.auctionId = auctionId;
    }

    public UUID getAuctionId() {
        return auctionId;
    }
}
