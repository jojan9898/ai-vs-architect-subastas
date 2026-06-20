package com.auctionbackend.domain.exception;

import java.math.BigDecimal;

/**
 * Domain exception: a bid was not strictly greater than the current highest.
 * Maps to HTTP 400 (the client's request violated a business rule).
 */
public final class BidTooLowException extends RuntimeException {

    private final BigDecimal currentHighest;
    private final BigDecimal attempted;

    public BidTooLowException(BigDecimal currentHighest, BigDecimal attempted) {
        super("Bid " + attempted + " must be strictly greater than the current highest " + currentHighest);
        this.currentHighest = currentHighest;
        this.attempted = attempted;
    }

    public BigDecimal getCurrentHighest() {
        return currentHighest;
    }

    public BigDecimal getAttempted() {
        return attempted;
    }
}
