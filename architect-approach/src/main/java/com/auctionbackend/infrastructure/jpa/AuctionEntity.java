package com.auctionbackend.infrastructure.jpa;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * JPA entity for an auction. Persistence concern only; the domain model is
 * {@link com.auctionbackend.domain.Auction}. The {@code @Version} field gives
 * us optimistic locking: the UPDATE statement becomes
 * {@code UPDATE ... WHERE id = ? AND version = ?}, so a concurrent mutation
 * updates zero rows and JPA throws instead of silently overwriting.
 */
@Entity
@Table(name = "auctions")
class AuctionEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    private String title;

    private BigDecimal highestBidAmount;
    private String highestBidUserId;
    private Instant highestBidTimestamp;

    protected AuctionEntity() {
        // no-arg constructor required by JPA
    }

    UUID getId() {
        return id;
    }

    void setId(UUID id) {
        this.id = id;
    }

    Long getVersion() {
        return version;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    BigDecimal getHighestBidAmount() {
        return highestBidAmount;
    }

    void setHighestBidAmount(BigDecimal highestBidAmount) {
        this.highestBidAmount = highestBidAmount;
    }

    String getHighestBidUserId() {
        return highestBidUserId;
    }

    void setHighestBidUserId(String highestBidUserId) {
        this.highestBidUserId = highestBidUserId;
    }

    Instant getHighestBidTimestamp() {
        return highestBidTimestamp;
    }

    void setHighestBidTimestamp(Instant highestBidTimestamp) {
        this.highestBidTimestamp = highestBidTimestamp;
    }
}
