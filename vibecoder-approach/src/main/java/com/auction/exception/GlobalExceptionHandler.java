package com.auction.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuctionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(AuctionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Auction not found", "message", ex.getMessage()));
    }

    @ExceptionHandler(BidNotHigherException.class)
    public ResponseEntity<Map<String, Object>> handleBidNotHigher(BidNotHigherException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "Bid not higher",
                        "message", ex.getMessage(),
                        "currentHighestBid", ex.getCurrentHighestBid()
                ));
    }
}
