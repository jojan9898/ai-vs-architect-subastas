package com.auction.controller;

import com.auction.domain.Auction;
import com.auction.service.AuctionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @PostMapping("/{id}/bid")
    public ResponseEntity<AuctionResponse> placeBid(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam String userId) {
        Auction updated = auctionService.placeBid(id, amount, userId);
        return ResponseEntity.ok(AuctionResponse.from(updated));
    }
}
