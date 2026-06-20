package com.auctionbackend.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auctionbackend.domain.Auction;
import com.auctionbackend.domain.AuctionRepository;

@ExtendWith(MockitoExtension.class)
class ListAuctionsUseCaseTest {

    @Mock
    AuctionRepository repository;

    @InjectMocks
    ListAuctionsUseCase useCase;

    @Test
    void returns_all_auctions_from_repository() {
        Auction one = new Auction(UUID.randomUUID(), "Laptop Gaming");
        Auction two = new Auction(UUID.randomUUID(), "Phone");
        two.placeBid(new BigDecimal("50"), "alice", java.time.Instant.now());
        when(repository.findAll()).thenReturn(List.of(one, two));

        List<Auction> result = useCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Laptop Gaming");
        assertThat(result.get(1).currentHighestAmount()).isEqualByComparingTo(new BigDecimal("50"));
    }
}
