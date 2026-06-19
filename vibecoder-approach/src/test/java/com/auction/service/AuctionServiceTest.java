package com.auction.service;

import com.auction.domain.Auction;
import com.auction.exception.AuctionNotFoundException;
import com.auction.exception.BidNotHigherException;
import com.auction.repository.AuctionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @InjectMocks
    private AuctionService auctionService;

    private Auction auction;

    @BeforeEach
    void setUp() {
        auction = new Auction("Laptop Gaming");
        when(auctionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void bid_higher_than_current_is_accepted() {
        Auction result = auctionService.placeBid(1L, new BigDecimal("100"), "user-1");

        assertThat(result.getCurrentHighestBid()).isEqualByComparingTo("100");
        assertThat(result.getHighestBidderUserId()).isEqualTo("user-1");
    }

    @Test
    void bid_equal_to_current_is_rejected() {
        auction.placeBid(new BigDecimal("50"), "user-0");

        assertThatThrownBy(() -> auctionService.placeBid(1L, new BigDecimal("50"), "user-1"))
                .isInstanceOf(BidNotHigherException.class);
    }

    @Test
    void bid_lower_than_current_is_rejected() {
        auction.placeBid(new BigDecimal("100"), "user-0");

        assertThatThrownBy(() -> auctionService.placeBid(1L, new BigDecimal("50"), "user-1"))
                .isInstanceOf(BidNotHigherException.class);
    }

    @Test
    void unknown_auction_throws_not_found() {
        when(auctionRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.placeBid(99L, new BigDecimal("100"), "user-1"))
                .isInstanceOf(AuctionNotFoundException.class);
    }
}
