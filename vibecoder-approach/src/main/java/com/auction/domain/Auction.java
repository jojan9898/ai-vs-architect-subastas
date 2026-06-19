package com.auction.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

@Entity
@Table(name = "auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private BigDecimal currentHighestBid = BigDecimal.ZERO;

    private String highestBidderUserId;

    @Version
    private Long version;

    protected Auction() {
    }

    public Auction(String title) {
        this.title = title;
        this.currentHighestBid = BigDecimal.ZERO;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }

    public String getHighestBidderUserId() {
        return highestBidderUserId;
    }

    public void placeBid(BigDecimal amount, String userId) {
        this.currentHighestBid = amount;
        this.highestBidderUserId = userId;
    }
}
