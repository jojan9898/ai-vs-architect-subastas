package com.auction.controller;

import com.auction.domain.Auction;
import com.auction.exception.AuctionNotFoundException;
import com.auction.exception.BidNotHigherException;
import com.auction.service.AuctionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuctionController.class)
class AuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuctionService auctionService;

    @Test
    void valid_bid_returns_200() throws Exception {
        Auction auction = new Auction("Laptop Gaming");
        auction.placeBid(new BigDecimal("100"), "user-1");
        when(auctionService.placeBid(1L, new BigDecimal("100"), "user-1")).thenReturn(auction);

        mockMvc.perform(post("/auctions/1/bid")
                        .param("amount", "100")
                        .param("userId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentHighestBid").value(100));
    }

    @Test
    void bid_not_higher_returns_400() throws Exception {
        when(auctionService.placeBid(1L, new BigDecimal("10"), "user-1"))
                .thenThrow(new BidNotHigherException(new BigDecimal("50")));

        mockMvc.perform(post("/auctions/1/bid")
                        .param("amount", "10")
                        .param("userId", "user-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void auction_not_found_returns_404() throws Exception {
        when(auctionService.placeBid(99L, new BigDecimal("100"), "user-1"))
                .thenThrow(new AuctionNotFoundException(99L));

        mockMvc.perform(post("/auctions/99/bid")
                        .param("amount", "100")
                        .param("userId", "user-1"))
                .andExpect(status().isNotFound());
    }
}
