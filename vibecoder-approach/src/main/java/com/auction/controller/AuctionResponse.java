package com.auction.controller;

import com.auction.domain.Auction;

import java.math.BigDecimal;

public record AuctionResponse(
        Long id,
        String title,
        BigDecimal currentHighestBid,
        String highestBidderUserId) {

    public static AuctionResponse from(Auction auction) {
        return new AuctionResponse(
                auction.getId(),
                auction.getTitle(),
                auction.getCurrentHighestBid(),
                auction.getHighestBidderUserId());
    }
}
