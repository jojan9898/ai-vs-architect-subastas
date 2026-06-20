package com.auctionbackend.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.auctionbackend.domain.exception.BidTooLowException;

/**
 * Aggregate Root. Owns the invariant: a new bid must be strictly greater than
 * the current highest bid (which starts at zero). State only changes through
 * the {@link #placeBid} behavior method; there are no setters that could
 * break the rule from the outside.
 */
public final class Auction {

    private final UUID id;
    private final String title;
    private Bid highestBid; // null until the first bid; conceptually zero

    public Auction(UUID id, String title) {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        this.id = id;
        this.title = title;
    }

    private Auction(UUID id, String title, Bid highestBid) {
        this.id = id;
        this.title = title;
        this.highestBid = highestBid;
    }

    /**
     * Restores an auction from persisted state without re-running the bidding
     * rule. The data was valid when it was saved, so reconstitution bypasses
     * the invariant. Use the public constructor only for brand-new auctions.
     */
    public static Auction reconstitute(UUID id, String title, Bid highestBid) {
        if (id == null) {
            throw new IllegalArgumentException("id is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        return new Auction(id, title, highestBid);
    }

    /**
     * The single place where the bidding invariant is enforced. Throws a domain
     * exception when the amount does not strictly beat the current highest.
     */
    public void placeBid(BigDecimal amount, String userId, Instant now) {
        if (amount == null) {
            throw new IllegalArgumentException("amount is required");
        }
        BigDecimal current = currentHighestAmount();
        if (amount.compareTo(current) <= 0) {
            throw new BidTooLowException(current, amount);
        }
        this.highestBid = new Bid(amount, userId, now);
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Optional<Bid> getHighestBid() {
        return Optional.ofNullable(highestBid);
    }

    /**
     * The current highest amount, zero when no bid has been placed yet.
     * The highest bid can only increase, never decrease.
     */
    public BigDecimal currentHighestAmount() {
        return highestBid == null ? BigDecimal.ZERO : highestBid.amount();
    }
}
