package com.auctionbackend.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Immutable Value Object representing a bid placed on an auction.
 * Equality is by value (record). Validity is enforced in the compact constructor.
 */
public record Bid(BigDecimal amount, String userId, java.time.Instant timestamp) {

    public Bid {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be strictly positive");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp is required");
        }
    }
}
