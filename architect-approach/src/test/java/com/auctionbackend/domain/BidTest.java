package com.auctionbackend.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class BidTest {

    private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

    @Test
    void valid_bid_is_created() {
        Bid bid = new Bid(new BigDecimal("150"), "alice", NOW);

        assertThat(bid.amount()).isEqualByComparingTo(new BigDecimal("150"));
        assertThat(bid.userId()).isEqualTo("alice");
        assertThat(bid.timestamp()).isEqualTo(NOW);
    }

    @Test
    void rejects_non_positive_amount() {
        assertThatThrownBy(() -> new Bid(BigDecimal.ZERO, "alice", NOW))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new Bid(new BigDecimal("-1"), "alice", NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null_amount() {
        assertThatThrownBy(() -> new Bid(null, "alice", NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_blank_user_id() {
        assertThatThrownBy(() -> new Bid(new BigDecimal("10"), " ", NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_null_timestamp() {
        assertThatThrownBy(() -> new Bid(new BigDecimal("10"), "alice", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality_is_by_value() {
        Bid a = new Bid(new BigDecimal("10"), "alice", NOW);
        Bid b = new Bid(new BigDecimal("10"), "alice", NOW);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
