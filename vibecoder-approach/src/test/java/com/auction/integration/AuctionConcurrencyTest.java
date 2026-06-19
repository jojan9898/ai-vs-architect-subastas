package com.auction.integration;

import com.auction.domain.Auction;
import com.auction.repository.AuctionRepository;
import com.auction.service.AuctionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuctionConcurrencyTest {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private AuctionRepository auctionRepository;

    private Long auctionId;

    @BeforeEach
    void setUp() {
        auctionRepository.deleteAll();
        Auction auction = auctionRepository.save(new Auction("Laptop Gaming"));
        auctionId = auction.getId();
    }

    @Test
    void only_one_bid_accepted_when_concurrent_requests() throws InterruptedException {
        int threads = 50;
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            final String userId = "user-" + i;
            executor.submit(() -> {
                try {
                    latch.await();
                    auctionService.placeBid(auctionId, new BigDecimal("1"), userId);
                    successes.incrementAndGet();
                } catch (Exception ignored) {
                }
            });
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS);

        assertThat(successes.get()).isEqualTo(1);

        Auction result = auctionRepository.findById(auctionId).orElseThrow();
        assertThat(result.getCurrentHighestBid()).isEqualByComparingTo("1");
    }
}
