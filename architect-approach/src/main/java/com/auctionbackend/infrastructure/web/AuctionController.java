package com.auctionbackend.infrastructure.web;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auctionbackend.application.ListAuctionsUseCase;
import com.auctionbackend.application.PlaceBidUseCase;
import com.auctionbackend.domain.Auction;

/**
 * Thin HTTP shell. It parses the request, calls a use case, and maps the
 * domain result to a response. Zero business rules live here.
 */
@RestController
@RequestMapping("/auctions")
public class AuctionController {

    private final PlaceBidUseCase placeBidUseCase;
    private final ListAuctionsUseCase listAuctionsUseCase;

    public AuctionController(PlaceBidUseCase placeBidUseCase, ListAuctionsUseCase listAuctionsUseCase) {
        this.placeBidUseCase = placeBidUseCase;
        this.listAuctionsUseCase = listAuctionsUseCase;
    }

    @GetMapping
    public List<AuctionResponse> list() {
        return listAuctionsUseCase.execute().stream()
                .map(AuctionResponse::from)
                .toList();
    }

    @PostMapping("/{id}/bids")
    public ResponseEntity<AuctionResponse> placeBid(@PathVariable UUID id, @Valid @RequestBody BidRequest request) {
        Auction updated = placeBidUseCase.execute(id, request.amount(), request.userId());
        return ResponseEntity.ok(AuctionResponse.from(updated));
    }
}
