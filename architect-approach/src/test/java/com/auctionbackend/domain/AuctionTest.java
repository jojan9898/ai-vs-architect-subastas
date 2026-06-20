package com.auctionbackend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.auctionbackend.domain.exception.BidTooLowException;

class AuctionTest {

    private static final UUID ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

    @Test
    void current_highest_starts_at_zero() {
        Auction auction = new Auction(ID, "Laptop Gaming");

        assertThat(auction.currentHighestAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(auction.getHighestBid()).isEmpty();
    }

    @Test
    void first_bid_must_be_strictly_greater_than_zero() {
        Auction auction = new Auction(ID, "Laptop Gaming");

        assertThatThrownBy(() -> auction.placeBid(BigDecimal.ZERO, "alice", NOW))
                .isInstanceOf(BidTooLowException.class);

        assertThatThrownBy(() -> auction.placeBid(new BigDecimal("-5"), "alice", NOW))
                .isInstanceOf(BidTooLowException.class);
    }

    @Test
    void first_positive_bid_succeeds() {
        Auction auction = new Auction(ID, "Laptop Gaming");

        auction.placeBid(new BigDecimal("100"), "alice", NOW);

        assertThat(auction.currentHighestAmount()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(auction.getHighestBid()).isPresent();
        assertThat(auction.getHighestBid().get().userId()).isEqualTo("alice");
    }

    @Test
    void bid_equal_to_current_is_rejected() {
        Auction auction = new Auction(ID, "Laptop Gaming");
        auction.placeBid(new BigDecimal("100"), "alice", NOW);

        assertThatThrownBy(() -> auction.placeBid(new BigDecimal("100"), "bob", NOW))
                .isInstanceOf(BidTooLowException.class);
    }

    @Test
    void bid_lower_than_current_is_rejected() {
        Auction auction = new Auction(ID, "Laptop Gaming");
        auction.placeBid(new BigDecimal("100"), "alice", NOW);

        assertThatThrownBy(() -> auction.placeBid(new BigDecimal("50"), "bob", NOW))
                .isInstanceOf(BidTooLowException.class);
    }

    @Test
    void highest_bid_can_only_increase() {
        Auction auction = new Auction(ID, "Laptop Gaming");
        auction.placeBid(new BigDecimal("100"), "alice", NOW);
        auction.placeBid(new BigDecimal("250"), "bob", NOW.plusSeconds(1));
        auction.placeBid(new BigDecimal("999"), "carol", NOW.plusSeconds(2));

        assertThat(auction.currentHighestAmount()).isEqualByComparingTo(new BigDecimal("999"));
        assertThat(auction.getHighestBid().get().userId()).isEqualTo("carol");
    }

    @Test
    void reconstitute_restores_highest_bid_without_revalidating() {
        Bid persisted = new Bid(new BigDecimal("500"), "alice", NOW);
        Auction auction = Auction.reconstitute(ID, "Laptop Gaming", persisted);

        assertThat(auction.currentHighestAmount()).isEqualByComparingTo(new BigDecimal("500"));
        assertThat(auction.getHighestBid()).contains(persisted);
    }

    @Test
    void constructor_rejects_blank_title() {
        assertThatThrownBy(() -> new Auction(ID, " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_rejects_null_id() {
        assertThatThrownBy(() -> new Auction(null, "Laptop Gaming"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
