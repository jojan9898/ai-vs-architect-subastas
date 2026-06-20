package com.auctionbackend.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.auctionbackend.domain.Auction;
import com.auctionbackend.domain.AuctionRepository;
import com.auctionbackend.domain.exception.AuctionNotFoundException;
import com.auctionbackend.domain.exception.BidTooLowException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlaceBidUseCaseTest {

    private static final UUID AUCTION_ID = UUID.randomUUID();
    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-01T00:00:00Z");

    @Mock
    AuctionRepository repository;

    @Mock
    Clock clock;

    @InjectMocks
    PlaceBidUseCase useCase;

    @BeforeEach
    void stubClock() {
        when(clock.instant()).thenReturn(FIXED_INSTANT);
    }

    @Test
    void throws_when_auction_does_not_exist() {
        when(repository.findById(AUCTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(AUCTION_ID, new BigDecimal("100"), "alice"))
                .isInstanceOf(AuctionNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void rejects_bid_not_strictly_higher() {
        Auction auction = new Auction(AUCTION_ID, "Laptop Gaming");
        auction.placeBid(new BigDecimal("100"), "alice", FIXED_INSTANT);
        when(repository.findById(AUCTION_ID)).thenReturn(Optional.of(auction));

        assertThatThrownBy(() -> useCase.execute(AUCTION_ID, new BigDecimal("100"), "bob"))
                .isInstanceOf(BidTooLowException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void saves_and_returns_auction_on_winning_bid() {
        Auction auction = new Auction(AUCTION_ID, "Laptop Gaming");
        when(repository.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(repository.save(any(Auction.class))).thenAnswer(inv -> inv.getArgument(0));

        Auction result = useCase.execute(AUCTION_ID, new BigDecimal("150"), "alice");

        assertThat(result.currentHighestAmount()).isEqualByComparingTo(new BigDecimal("150"));
        assertThat(result.getHighestBid().get().userId()).isEqualTo("alice");
        assertThat(result.getHighestBid().get().timestamp()).isEqualTo(FIXED_INSTANT);
        verify(repository).save(auction);
    }

    @Test
    void uses_clock_for_bid_timestamp() {
        Instant later = FIXED_INSTANT.plusSeconds(60);
        when(clock.instant()).thenReturn(later);
        Auction auction = new Auction(AUCTION_ID, "Laptop Gaming");
        when(repository.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(repository.save(any(Auction.class))).thenAnswer(inv -> inv.getArgument(0));

        Auction result = useCase.execute(AUCTION_ID, new BigDecimal("10"), "alice");

        assertThat(result.getHighestBid().get().timestamp()).isEqualTo(later);
    }
}
