package com.auction.exception;

import java.math.BigDecimal;

public class BidNotHigherException extends RuntimeException {

    private final BigDecimal currentHighestBid;

    public BidNotHigherException(BigDecimal currentHighestBid) {
        super("Bid amount must be higher than the current highest bid: " + currentHighestBid);
        this.currentHighestBid = currentHighestBid;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }
}
