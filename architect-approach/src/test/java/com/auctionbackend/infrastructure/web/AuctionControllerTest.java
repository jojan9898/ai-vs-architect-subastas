package com.auctionbackend.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.auctionbackend.application.ListAuctionsUseCase;
import com.auctionbackend.application.PlaceBidUseCase;
import com.auctionbackend.domain.Auction;
import com.auctionbackend.domain.exception.AuctionNotFoundException;
import com.auctionbackend.domain.exception.BidTooLowException;

@WebMvcTest(AuctionController.class)
class AuctionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PlaceBidUseCase placeBidUseCase;

    @MockBean
    ListAuctionsUseCase listAuctionsUseCase;

    @Test
    void list_returns_all_auctions() throws Exception {
        UUID id = UUID.fromString("c0a80001-0000-0000-0000-000000000001");
        Auction auction = new Auction(id, "Laptop Gaming");
        when(listAuctionsUseCase.execute()).thenReturn(List.of(auction));

        mockMvc.perform(get("/auctions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].title").value("Laptop Gaming"))
                .andExpect(jsonPath("$[0].highestBid").value(0));
    }

    @Test
    void place_bid_returns_200_on_winning_bid() throws Exception {
        UUID id = UUID.randomUUID();
        Auction auction = Auction.reconstitute(id, "Laptop Gaming",
                new com.auctionbackend.domain.Bid(new BigDecimal("150"), "alice", Instant.parse("2025-01-01T00:00:00Z")));
        when(placeBidUseCase.execute(eq(id), any(BigDecimal.class), eq("alice"))).thenReturn(auction);

        mockMvc.perform(post("/auctions/{id}/bids", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":150,"userId":"alice"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highestBid").value(150))
                .andExpect(jsonPath("$.highestBidUserId").value("alice"));
    }

    @Test
    void place_bid_returns_400_when_bid_not_higher() throws Exception {
        UUID id = UUID.randomUUID();
        when(placeBidUseCase.execute(eq(id), any(BigDecimal.class), any(String.class)))
                .thenThrow(new BidTooLowException(new BigDecimal("100"), new BigDecimal("100")));

        mockMvc.perform(post("/auctions/{id}/bids", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":100,"userId":"bob"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BID_TOO_LOW"));
    }

    @Test
    void place_bid_returns_404_when_auction_not_found() throws Exception {
        UUID id = UUID.randomUUID();
        when(placeBidUseCase.execute(eq(id), any(BigDecimal.class), any(String.class)))
                .thenThrow(new AuctionNotFoundException(id));

        mockMvc.perform(post("/auctions/{id}/bids", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":100,"userId":"alice"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AUCTION_NOT_FOUND"));
    }

    @Test
    void place_bid_returns_400_when_amount_is_missing() throws Exception {
        mockMvc.perform(post("/auctions/{id}/bids", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"alice"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void place_bid_returns_400_when_user_id_is_blank() throws Exception {
        mockMvc.perform(post("/auctions/{id}/bids", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":100,"userId":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
